package com.app.payment.application.usecase

import com.app.payment.application.dto.CallbackAuthCommand
import com.app.payment.application.dto.TossApiResponse
import com.app.payment.application.port.TossPaymentGateway
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class CallbackAuthUseCase(
    private val tossPaymentGateway: TossPaymentGateway
) {
    @Throws(IOException::class)
    fun requestAccessToken(command: CallbackAuthCommand): TossApiResponse {
        return tossPaymentGateway.requestBrandpayAccessToken(command)
    }
}
