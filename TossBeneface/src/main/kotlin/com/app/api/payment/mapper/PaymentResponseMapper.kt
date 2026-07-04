package com.app.api.payment.mapper

import com.app.api.payment.dto.PaymentConfirmSuccessResponse
import com.app.api.payment.dto.PaymentErrorResponse
import com.app.api.payment.dto.PaymentResponse
import com.app.api.payment.dto.TossApiBodyResponse
import com.app.payment.application.dto.PaymentResult
import com.app.payment.application.dto.TossApiResponse
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class PaymentResponseMapper {

    fun toResponseEntity(result: PaymentResult): ResponseEntity<PaymentResponse> {
        return when (result) {
            is PaymentResult.Success -> {
                val confirmed = result.result
                ResponseEntity.ok(
                    PaymentConfirmSuccessResponse(
                        status = confirmed.status,
                        paymentKey = confirmed.paymentKey,
                        orderId = confirmed.orderId,
                        totalAmount = confirmed.totalAmount
                    )
                )
            }
            is PaymentResult.Failure -> {
                ResponseEntity.status(400).body(
                    PaymentErrorResponse(
                        errorCode = result.errorCode,
                        errorMessage = result.message
                    )
                )
            }
        }
    }

    fun toResponseEntity(response: TossApiResponse): ResponseEntity<PaymentResponse> {
        return ResponseEntity
            .status(response.statusCode)
            .body(TossApiBodyResponse(response.body))
    }
}
