package com.app.payment.infra.store

import com.app.domain.payment.entity.Payment
import com.app.domain.payment.repository.PaymentRepository
import com.app.payment.application.port.PaymentStore
import org.springframework.stereotype.Component

@Component
class JpaPaymentStore(
    private val paymentRepository: PaymentRepository
) : PaymentStore {

    override fun save(payment: Payment): Payment {
        return paymentRepository.save(payment)
    }
}
