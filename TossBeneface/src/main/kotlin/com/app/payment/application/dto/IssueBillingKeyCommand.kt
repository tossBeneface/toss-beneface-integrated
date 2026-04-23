package com.app.payment.application.dto

import jakarta.validation.constraints.NotBlank

data class IssueBillingKeyCommand(
    @field:NotBlank val customerKey: String,
    @field:NotBlank val authKey: String
)
