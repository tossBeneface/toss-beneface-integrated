package com.app.payment.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ConfirmPaymentCommand(
    @field:NotBlank val paymentKey: String,
    @field:NotBlank val orderId: String,
    @field:NotNull val amount: Int,
    val memberId: Long? = null
) {
    fun withMemberId(memberId: Long): ConfirmPaymentCommand {
        return copy(memberId = memberId)
    }
}
