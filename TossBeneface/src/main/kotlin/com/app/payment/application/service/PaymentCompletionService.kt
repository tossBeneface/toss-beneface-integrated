package com.app.payment.application.service

import com.app.domain.member.repository.MemberRepository
import com.app.domain.payment.entity.Payment
import com.app.global.error.ErrorCode
import com.app.global.error.exception.EntityNotFoundException
import com.app.global.kafka.event.PaymentCompletedEvent
import com.app.global.kafka.outbox.OutboxEvent
import com.app.global.kafka.outbox.OutboxRepository
import com.app.payment.application.dto.ConfirmedPaymentResult
import com.app.payment.application.port.PaymentStore
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentCompletionService(
    private val paymentStore: PaymentStore,
    private val memberRepository: MemberRepository,
    private val outboxRepository: OutboxRepository,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun savePayment(confirmedPayment: ConfirmedPaymentResult) {
        val member = memberRepository.findByIdWithPessimisticLock(confirmedPayment.memberId)
            .orElseThrow { EntityNotFoundException(ErrorCode.MEMBER_NOT_EXIST) }

        val payment = Payment(
            paymentKey = confirmedPayment.paymentKey,
            orderId = confirmedPayment.orderId,
            orderName = confirmedPayment.orderName,
            method = confirmedPayment.method,
            totalAmount = confirmedPayment.totalAmount,
            status = confirmedPayment.status,
            requestedAt = confirmedPayment.requestedAt,
            approvedAt = confirmedPayment.approvedAt,
            receiptUrl = confirmedPayment.receiptUrl,
            member = member
        )

        val savedPayment = paymentStore.save(payment)

        member.checkAndSubtractBudget(savedPayment.totalAmount)

        val paymentId = savedPayment.id ?: throw IllegalStateException("Payment id is missing")
        val memberId = member.memberId ?: throw IllegalStateException("Member id is missing")

        val paymentCompletedEvent = PaymentCompletedEvent(
            paymentId = paymentId,
            paymentKey = savedPayment.paymentKey,
            memberId = memberId,
            memberName = member.memberName,
            totalAmount = savedPayment.totalAmount,
            method = savedPayment.method,
            approvedAt = savedPayment.approvedAt
        )

        outboxRepository.save(
            OutboxEvent(
                aggregateId = paymentId.toString(),
                aggregateType = "PAYMENT",
                eventType = "PAYMENT_COMPLETED",
                partitionKey = memberId.toString(),
                payload = objectMapper.writeValueAsString(paymentCompletedEvent)
            )
        )
    }
}
