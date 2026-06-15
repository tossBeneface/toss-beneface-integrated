package com.app.payment.application.usecase

import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import com.app.payment.application.PaymentConfirmType
import com.app.payment.application.dto.ConfirmPaymentCommand
import com.app.payment.application.dto.PaymentResult
import com.app.payment.application.port.TossPaymentGateway
import com.app.payment.application.service.PaymentCompletionService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ConfirmPaymentUseCase(
    private val tossPaymentGateway: TossPaymentGateway,
    private val paymentCompletionService: PaymentCompletionService
) {

    @Transactional
    fun confirm(command: ConfirmPaymentCommand, confirmType: PaymentConfirmType): PaymentResult {
        val result = tossPaymentGateway.confirmPayment(command, confirmType)

        if (result is PaymentResult.Success) {
            if (result.result.memberId <= 0) {
                throw BusinessException(ErrorCode.MEMBER_NOT_EXIST)
            }
            paymentCompletionService.savePayment(result.result)
        }

        return result
    }
}
