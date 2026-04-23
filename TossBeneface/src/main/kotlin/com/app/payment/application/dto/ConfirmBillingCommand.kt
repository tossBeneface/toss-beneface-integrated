package com.app.payment.application.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class ConfirmBillingCommand(
    @field:NotBlank val customerKey: String,
    @field:NotNull val amount: Int,
    @field:NotBlank val orderId: String,
    @field:NotBlank val orderName: String,
    @field:Email @field:NotBlank val customerEmail: String,
    @field:NotBlank val customerName: String
)
