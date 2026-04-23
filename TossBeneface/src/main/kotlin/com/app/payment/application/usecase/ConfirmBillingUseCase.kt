package com.app.payment.application.usecase

import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import com.app.payment.application.dto.ConfirmBillingCommand
import com.app.payment.application.dto.TossApiResponse
import com.app.payment.application.port.BillingKeyStore
import com.app.payment.application.port.TossPaymentGateway
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class ConfirmBillingUseCase(
    private val tossPaymentGateway: TossPaymentGateway,
    private val billingKeyStore: BillingKeyStore
) {
    @Throws(IOException::class)
    fun confirm(command: ConfirmBillingCommand): TossApiResponse {
        val billingKey = billingKeyStore.find(command.customerKey)
            .orElseThrow { BusinessException(ErrorCode.BILLING_KEY_NOT_FOUND) }
        return tossPaymentGateway.confirmBilling(command, billingKey)
    }
}
