package com.app.payment.application.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ConfirmBrandpayCommand(
    @field:NotBlank val paymentKey: String,
    @field:NotBlank val orderId: String,
    @field:NotNull val amount: Int,
    @field:NotBlank val customerKey: String
)
