package com.app.payment.application.port

import com.app.domain.payment.entity.Payment

interface PaymentStore {
    fun save(payment: Payment): Payment
}
