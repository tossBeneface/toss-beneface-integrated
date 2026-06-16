package com.app.payment.application.service

import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import com.app.payment.application.dto.ConfirmedPaymentResult
import org.springframework.stereotype.Component

@Component
class PaymentConfirmationValidator {

    fun validate(confirmedPayment: ConfirmedPaymentResult) {
        if (confirmedPayment.memberId <= 0) {
            throw BusinessException(ErrorCode.MEMBER_NOT_EXIST)
        }
    }
}
