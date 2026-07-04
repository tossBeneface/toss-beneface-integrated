package com.app.payment.infra.toss

import com.app.domain.payment.entity.PaymentStatus
import com.app.payment.application.dto.PaymentResult
import com.app.payment.application.dto.TossApiResponse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class TossPaymentResponseMapperTest {

    private val mapper = TossPaymentResponseMapper()

    @Test
    fun `maps successful Toss response to confirmed payment result`() {
        val response = TossApiResponse(
            statusCode = 200,
            body = mapOf(
                "paymentKey" to "payment-key",
                "orderId" to "order-id",
                "orderName" to "order-name",
                "method" to "card",
                "totalAmount" to 10_000,
                "status" to "DONE",
                "requestedAt" to "2026-06-16T01:00:00+09:00",
                "approvedAt" to "2026-06-16T01:01:00+09:00",
                "receipt" to mapOf("url" to "https://receipt.example")
            )
        )

        val result = assertInstanceOf(
            PaymentResult.Success::class.java,
            mapper.toPaymentResult(response, 42L)
        )

        assertEquals(42L, result.result.memberId)
        assertEquals("payment-key", result.result.paymentKey)
        assertEquals("order-id", result.result.orderId)
        assertEquals("order-name", result.result.orderName)
        assertEquals("card", result.result.method)
        assertEquals(10_000, result.result.totalAmount)
        assertEquals(PaymentStatus.DONE, result.result.status)
        assertEquals("2026-06-16T01:00:00+09:00", result.result.requestedAt)
        assertEquals("2026-06-16T01:01:00+09:00", result.result.approvedAt)
        assertEquals("https://receipt.example", result.result.receiptUrl)
    }

    @Test
    fun `uses Toss body member id when command member id is missing`() {
        val response = TossApiResponse(
            statusCode = 200,
            body = mapOf(
                "memberId" to "99",
                "paymentKey" to "payment-key",
                "orderId" to "order-id",
                "orderName" to "order-name",
                "method" to "card",
                "totalAmount" to "10000",
                "status" to "DONE"
            )
        )

        val result = assertInstanceOf(
            PaymentResult.Success::class.java,
            mapper.toPaymentResult(response, null)
        )

        assertEquals(99L, result.result.memberId)
        assertNull(result.result.receiptUrl)
    }

    @Test
    fun `maps failed Toss response to failure`() {
        val response = TossApiResponse(
            statusCode = 400,
            body = mapOf(
                "code" to "INVALID_REQUEST",
                "message" to "invalid amount"
            )
        )

        val result = assertInstanceOf(
            PaymentResult.Failure::class.java,
            mapper.toPaymentResult(response, 42L)
        )

        assertEquals("INVALID_REQUEST", result.errorCode)
        assertEquals("invalid amount", result.message)
    }

    @Test
    fun `maps failed Toss response with missing fields to defaults`() {
        val response = TossApiResponse(statusCode = 500, body = emptyMap())

        val result = assertInstanceOf(
            PaymentResult.Failure::class.java,
            mapper.toPaymentResult(response, 42L)
        )

        assertEquals("UNKNOWN_ERROR", result.errorCode)
        assertEquals("Toss API Error", result.message)
    }
}
