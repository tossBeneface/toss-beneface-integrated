package com.app.api.payment.dto

data class PaymentDto(
    var impuid: String? = null,
    var name: String? = null,
    var status: String? = null,
    var amount: Long? = null
)
