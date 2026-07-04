package com.app.payment.infra.toss

import com.app.global.config.TossPaymentConfig
import com.app.payment.application.PaymentConfirmType
import com.app.payment.application.dto.*
import com.app.payment.application.port.TossPaymentGateway
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class TossPaymentHttpClient(
    private val tossHttpClient: TossHttpClient,
    private val tossPaymentConfig: TossPaymentConfig,
    private val tossPaymentResponseMapper: TossPaymentResponseMapper
) : TossPaymentGateway {

    private val log = LoggerFactory.getLogger(TossPaymentHttpClient::class.java)
    private val tossApiBaseUrl = "https://api.tosspayments.com/v1"

    @CircuitBreaker(name = "tossPayment", fallbackMethod = "fallbackPayment")
    @Retry(name = "tossPayment")
    override fun confirmPayment(command: ConfirmPaymentCommand, confirmType: PaymentConfirmType): PaymentResult {
        val secretKey = if (confirmType == PaymentConfirmType.PAYMENT) {
            tossPaymentConfig.testSecretKey
        } else {
            tossPaymentConfig.testClientApiKey
        }

        val requestData = mapOf(
            "paymentKey" to command.paymentKey,
            "orderId" to command.orderId,
            "amount" to command.amount
        )

        val response = tossHttpClient.postJson(requestData, secretKey, "$tossApiBaseUrl/payments/confirm")
        return tossPaymentResponseMapper.toPaymentResult(response, command.memberId)
    }

    fun fallbackPayment(command: ConfirmPaymentCommand, confirmType: PaymentConfirmType, e: Throwable): PaymentResult {
        log.error("Toss Payment API fallback: {}", e.message)
        return PaymentResult.Failure("EXTERNAL_API_ERROR", "결제 서비스가 일시적으로 중단되었습니다. 잠시 후 다시 시도해주세요.")
    }

    override fun issueBillingKey(command: IssueBillingKeyCommand): TossApiResponse {
        return tossHttpClient.postJson(command, tossPaymentConfig.testSecretKey, "$tossApiBaseUrl/billing/authorizations/issue")
    }

    override fun confirmBilling(command: ConfirmBillingCommand, billingKey: String): TossApiResponse {
        return tossHttpClient.postJson(command, tossPaymentConfig.testSecretKey, "$tossApiBaseUrl/billing/$billingKey")
    }

    override fun requestBrandpayAccessToken(command: CallbackAuthCommand): TossApiResponse {
        val requestData = mapOf(
            "grantType" to "AuthorizationCode",
            "customerKey" to command.customerKey,
            "code" to command.code
        )
        return tossHttpClient.postJson(requestData, tossPaymentConfig.testSecretKey, "$tossApiBaseUrl/brandpay/authorizations/access-token")
    }

    override fun confirmBrandpay(command: ConfirmBrandpayCommand): TossApiResponse {
        return tossHttpClient.postJson(command, tossPaymentConfig.testSecretKey, "$tossApiBaseUrl/brandpay/payments/confirm")
    }
}
