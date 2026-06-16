package com.app.payment.application.service

import com.app.domain.payment.entity.PaymentStatus
import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import com.app.payment.application.dto.ConfirmedPaymentResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class PaymentConfirmationValidatorTest {

    private val validator = PaymentConfirmationValidator()

    @Test
    fun `does not throw when confirmed payment has member id`() {
        validator.validate(createConfirmedPayment(memberId = 1L))
    }

    @Test
    fun `throws member not exist when confirmed payment member id is missing`() {
        val exception = assertThrows(BusinessException::class.java) {
            validator.validate(createConfirmedPayment(memberId = 0L))
        }

        assertEquals(ErrorCode.MEMBER_NOT_EXIST, exception.errorCode)
    }

    private fun createConfirmedPayment(memberId: Long) = ConfirmedPaymentResult(
        memberId = memberId,
        paymentKey = "payment-key",
        orderId = "order-id",
        orderName = "order-name",
        method = "card",
        totalAmount = 10_000,
        status = PaymentStatus.DONE,
        requestedAt = "2026-06-16T01:00:00+09:00",
        approvedAt = "2026-06-16T01:01:00+09:00",
        receiptUrl = "https://receipt.example"
    )
}
