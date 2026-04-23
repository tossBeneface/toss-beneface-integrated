package com.app.payment.application.usecase

import com.app.payment.application.dto.IssueBillingKeyCommand
import com.app.payment.application.dto.TossApiResponse
import com.app.payment.application.port.BillingKeyStore
import com.app.payment.application.port.TossPaymentGateway
import org.springframework.stereotype.Service
import java.io.IOException

@Service
class IssueBillingKeyUseCase(
    private val tossPaymentGateway: TossPaymentGateway,
    private val billingKeyStore: BillingKeyStore
) {
    @Throws(IOException::class)
    fun issue(command: IssueBillingKeyCommand): TossApiResponse {
        val response = tossPaymentGateway.issueBillingKey(command)
        if (response.isSuccess()) {
            billingKeyStore.save(command.customerKey, response.getRequiredString("billingKey"))
        }
        return response
    }
}
