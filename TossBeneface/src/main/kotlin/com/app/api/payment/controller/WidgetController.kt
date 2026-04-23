package com.app.api.payment.controller

import com.app.payment.application.PaymentConfirmType
import com.app.payment.application.dto.ConfirmPaymentCommand
import com.app.payment.application.dto.PaymentResult
import com.app.payment.application.port.TossPaymentGateway
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
class WidgetController(
    private val tossPaymentGateway: TossPaymentGateway
) {

    @PostMapping("/confirm")
    @Throws(Exception::class)
    fun confirmPayment(
        @Valid @RequestBody command: ConfirmPaymentCommand
    ): ResponseEntity<Map<String, Any>> {
        val result = tossPaymentGateway.confirmPayment(command, PaymentConfirmType.WIDGET)

        return when (result) {
            is PaymentResult.Success -> {
                val confirmed = result.result
                ResponseEntity.ok(
                    mapOf(
                        "status" to confirmed.status.name,
                        "paymentKey" to confirmed.paymentKey,
                        "orderId" to confirmed.orderId,
                        "totalAmount" to confirmed.totalAmount
                    )
                )
            }
            is PaymentResult.Failure -> {
                ResponseEntity.status(400).body(
                    mapOf(
                        "errorCode" to result.errorCode,
                        "errorMessage" to result.message
                    )
                )
            }
            else -> ResponseEntity.internalServerError().build()
        }
    }
}
