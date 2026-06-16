package com.app.payment.application.usecase

import com.app.domain.payment.entity.PaymentStatus
import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import com.app.payment.application.PaymentConfirmType
import com.app.payment.application.dto.ConfirmPaymentCommand
import com.app.payment.application.dto.ConfirmedPaymentResult
import com.app.payment.application.dto.PaymentResult
import com.app.payment.application.port.TossPaymentGateway
import com.app.payment.application.service.PaymentConfirmationValidator
import com.app.payment.application.service.PaymentCompletionService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class ConfirmPaymentUseCaseTest {

    private val tossPaymentGateway = mockk<TossPaymentGateway>()
    private val paymentCompletionService = mockk<PaymentCompletionService>(relaxed = true)
    private val paymentConfirmationValidator = PaymentConfirmationValidator()
    private val useCase = ConfirmPaymentUseCase(
        tossPaymentGateway,
        paymentConfirmationValidator,
        paymentCompletionService
    )

    @Test
    fun `confirms payment and saves successful result`() {
        val command = createCommand()
        val confirmedPayment = createConfirmedPayment(memberId = 1L)
        every {
            tossPaymentGateway.confirmPayment(command, PaymentConfirmType.PAYMENT)
        } returns PaymentResult.Success(confirmedPayment)

        val result = useCase.confirm(command, PaymentConfirmType.PAYMENT)

        assertSame(confirmedPayment, (result as PaymentResult.Success).result)
        verify { paymentCompletionService.savePayment(confirmedPayment) }
    }

    @Test
    fun `returns Toss failure without saving payment`() {
        val command = createCommand()
        val failure = PaymentResult.Failure("INVALID_REQUEST", "invalid amount")
        every {
            tossPaymentGateway.confirmPayment(command, PaymentConfirmType.PAYMENT)
        } returns failure

        val result = useCase.confirm(command, PaymentConfirmType.PAYMENT)

        assertSame(failure, result)
        verify(exactly = 0) { paymentCompletionService.savePayment(any()) }
    }

    @Test
    fun `throws when successful result has invalid member id`() {
        val command = createCommand()
        val confirmedPayment = createConfirmedPayment(memberId = 0L)
        every {
            tossPaymentGateway.confirmPayment(command, PaymentConfirmType.PAYMENT)
        } returns PaymentResult.Success(confirmedPayment)

        val exception = assertThrows(BusinessException::class.java) {
            useCase.confirm(command, PaymentConfirmType.PAYMENT)
        }

        assertEquals(ErrorCode.MEMBER_NOT_EXIST, exception.errorCode)
        verify(exactly = 0) { paymentCompletionService.savePayment(any()) }
    }

    private fun createCommand() = ConfirmPaymentCommand(
        paymentKey = "payment-key",
        orderId = "order-id",
        amount = 10_000,
        memberId = 1L
    )

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
