package com.app.payment.application.dto

import jakarta.validation.constraints.NotBlank

data class CallbackAuthCommand(
    @field:NotBlank val customerKey: String,
    @field:NotBlank val code: String
)
