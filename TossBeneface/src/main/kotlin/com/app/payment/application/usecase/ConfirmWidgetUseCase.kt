package com.app.payment.application.usecase

import com.app.payment.application.PaymentConfirmType
import com.app.payment.application.dto.ConfirmPaymentCommand
import com.app.payment.application.dto.PaymentResult
import com.app.payment.application.port.TossPaymentGateway
import org.springframework.stereotype.Service

@Service
class ConfirmWidgetUseCase(
    private val tossPaymentGateway: TossPaymentGateway
) {

    fun confirm(command: ConfirmPaymentCommand): PaymentResult {
        return tossPaymentGateway.confirmPayment(command, PaymentConfirmType.WIDGET)
    }
}
