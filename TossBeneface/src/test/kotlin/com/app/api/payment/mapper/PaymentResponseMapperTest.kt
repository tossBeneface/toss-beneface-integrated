package com.app.api.payment.mapper

import com.app.api.payment.dto.PaymentConfirmSuccessResponse
import com.app.api.payment.dto.PaymentErrorResponse
import com.app.api.payment.dto.TossApiBodyResponse
import com.app.domain.payment.entity.PaymentStatus
import com.app.payment.application.dto.ConfirmedPaymentResult
import com.app.payment.application.dto.PaymentResult
import com.app.payment.application.dto.TossApiResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Test

class PaymentResponseMapperTest {

    private val mapper = PaymentResponseMapper()

    @Test
    fun mapsPaymentSuccess() {
        val response = mapper.toResponseEntity(
            PaymentResult.Success(
                ConfirmedPaymentResult(
                    memberId = 1L,
                    paymentKey = "payment-key",
                    orderId = "order-id",
                    orderName = "order-name",
                    method = "card",
                    totalAmount = 10_000,
                    status = PaymentStatus.DONE,
                    requestedAt = null,
                    approvedAt = null,
                    receiptUrl = null
                )
            )
        )

        assertEquals(200, response.statusCode.value())
        val body = assertInstanceOf(PaymentConfirmSuccessResponse::class.java, response.body)
        assertEquals(PaymentStatus.DONE, body.status)
        assertEquals("payment-key", body.paymentKey)
        assertEquals("order-id", body.orderId)
        assertEquals(10_000, body.totalAmount)
    }

    @Test
    fun mapsPaymentFailure() {
        val response = mapper.toResponseEntity(PaymentResult.Failure("ERROR", "failed"))

        assertEquals(400, response.statusCode.value())
        val body = assertInstanceOf(PaymentErrorResponse::class.java, response.body)
        assertEquals("ERROR", body.errorCode)
        assertEquals("failed", body.errorMessage)
    }

    @Test
    fun mapsTossApiBodyWithoutChangingStatusCode() {
        val response = mapper.toResponseEntity(
            TossApiResponse(
                statusCode = 202,
                body = mapOf("billingKey" to "billing-key")
            )
        )

        assertEquals(202, response.statusCode.value())
        val body = assertInstanceOf(TossApiBodyResponse::class.java, response.body)
        assertEquals("billing-key", body.fields["billingKey"])
    }
}
