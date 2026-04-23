package com.app.payment.application.usecase

import com.app.api.payment.service.PaymentService
import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import com.app.payment.application.PaymentConfirmType
import com.app.payment.application.dto.ConfirmPaymentCommand
import com.app.payment.application.dto.PaymentResult
import com.app.payment.application.port.TossPaymentGateway
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ConfirmPaymentUseCase(
    private val tossPaymentGateway: TossPaymentGateway,
    private val paymentService: PaymentService
) {

    @Transactional
    fun confirm(command: ConfirmPaymentCommand, confirmType: PaymentConfirmType): PaymentResult {
        // 1. 토스 페이먼츠 승인 API 호출
        val result = tossPaymentGateway.confirmPayment(command, confirmType)

        // 2. 승인 결과에 따른 비즈니스 처리
        if (result is PaymentResult.Success) {
            if (result.result.memberId <= 0) {
                throw BusinessException(ErrorCode.MEMBER_NOT_EXIST)
            }
            // 성공 시 DB 저장 및 회원 예산 차감
            paymentService.savePayment(result.result)
        }

        return result
    }
}
