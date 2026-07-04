package com.app.api.payment.controller

import com.app.api.payment.dto.PaymentResponse
import com.app.api.payment.mapper.PaymentResponseMapper
import com.app.payment.application.dto.ConfirmPaymentCommand
import com.app.payment.application.usecase.ConfirmWidgetUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
class WidgetController(
    private val confirmWidgetUseCase: ConfirmWidgetUseCase,
    private val paymentResponseMapper: PaymentResponseMapper
) {

    @PostMapping("/confirm")
    @Throws(Exception::class)
    fun confirmPayment(
        @Valid @RequestBody command: ConfirmPaymentCommand
    ): ResponseEntity<PaymentResponse> {
        return paymentResponseMapper.toResponseEntity(confirmWidgetUseCase.confirm(command))
    }
}
