package com.app.payment.application.service

import com.app.domain.member.repository.MemberRepository
import com.app.global.error.ErrorCode
import com.app.global.error.exception.EntityNotFoundException
import com.app.global.kafka.outbox.OutboxRepository
import com.app.payment.application.dto.ConfirmedPaymentResult
import com.app.payment.application.port.PaymentStore
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PaymentCompletionService(
    private val paymentStore: PaymentStore,
    private val memberRepository: MemberRepository,
    private val outboxRepository: OutboxRepository,
    private val paymentFactory: PaymentFactory,
    private val paymentCompletedEventFactory: PaymentCompletedEventFactory
) {

    @Transactional
    fun savePayment(confirmedPayment: ConfirmedPaymentResult) {
        val member = memberRepository.findByIdWithPessimisticLock(confirmedPayment.memberId)
            .orElseThrow { EntityNotFoundException(ErrorCode.MEMBER_NOT_EXIST) }

        val payment = paymentFactory.create(confirmedPayment, member)
        val savedPayment = paymentStore.save(payment)

        member.checkAndSubtractBudget(savedPayment.totalAmount)

        outboxRepository.save(paymentCompletedEventFactory.create(savedPayment, member))
    }
}
