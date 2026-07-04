package com.app.api.payment.dto

import com.app.domain.payment.entity.PaymentStatus
import com.fasterxml.jackson.annotation.JsonAnyGetter
import com.fasterxml.jackson.annotation.JsonIgnore

sealed interface PaymentResponse

data class PaymentConfirmSuccessResponse(
    val status: PaymentStatus,
    val paymentKey: String,
    val orderId: String,
    val totalAmount: Int
) : PaymentResponse

data class PaymentErrorResponse(
    val errorCode: String,
    val errorMessage: String
) : PaymentResponse

class TossApiBodyResponse(
    @get:JsonIgnore
    val fields: Map<String, Any>
) : PaymentResponse {

    @JsonAnyGetter
    fun properties(): Map<String, Any> {
        return fields
    }
}
