package com.app.payment.application.usecase

import com.app.payment.application.dto.ConfirmBrandpayCommand
import com.app.payment.application.dto.TossApiResponse
import com.app.payment.application.port.TossPaymentGateway
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class ConfirmBrandpayUseCase(
    private val tossPaymentGateway: TossPaymentGateway
) {
    @Throws(IOException::class)
    fun confirm(command: ConfirmBrandpayCommand): TossApiResponse {
        return tossPaymentGateway.confirmBrandpay(command)
    }
}
