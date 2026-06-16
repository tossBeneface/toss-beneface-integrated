package com.app.payment.application.service

import com.app.domain.member.entity.Member
import com.app.domain.payment.entity.Payment
import com.app.global.kafka.event.PaymentCompletedEvent
import com.app.global.kafka.outbox.OutboxEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component

@Component
class PaymentCompletedEventFactory(
    private val objectMapper: ObjectMapper
) {

    fun create(savedPayment: Payment, member: Member): OutboxEvent {
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

        return OutboxEvent(
            aggregateId = paymentId.toString(),
            aggregateType = "PAYMENT",
            eventType = "PAYMENT_COMPLETED",
            partitionKey = memberId.toString(),
            payload = objectMapper.writeValueAsString(paymentCompletedEvent)
        )
    }
}
