package com.app.payment.infra.toss

import com.app.domain.payment.entity.PaymentStatus
import com.app.global.config.TossPaymentConfig
import com.app.payment.application.PaymentConfirmType
import com.app.payment.application.dto.*
import com.app.payment.application.port.TossPaymentGateway
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import io.github.resilience4j.retry.annotation.Retry
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.*

@Component
class TossPaymentHttpClient(
    private val objectMapper: ObjectMapper,
    private val tossPaymentConfig: TossPaymentConfig
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

        val response = sendRequest(requestData, secretKey, "$tossApiBaseUrl/payments/confirm")
        return if (response.isSuccess()) {
            PaymentResult.Success(toConfirmedPaymentResult(response, command.memberId))
        } else {
            val errorCode = response.body["code"]?.toString() ?: "UNKNOWN_ERROR"
            val message = response.body["message"]?.toString() ?: "Toss API Error"
            PaymentResult.Failure(errorCode, message)
        }
    }

    fun fallbackPayment(command: ConfirmPaymentCommand, confirmType: PaymentConfirmType, e: Throwable): PaymentResult {
        log.error("Toss Payment API fallback: {}", e.message)
        return PaymentResult.Failure("EXTERNAL_API_ERROR", "결제 서비스가 일시적으로 중단되었습니다. 잠시 후 다시 시도해주세요.")
    }

    override fun issueBillingKey(command: IssueBillingKeyCommand): TossApiResponse {
        return sendRequest(command, tossPaymentConfig.testSecretKey, "$tossApiBaseUrl/billing/authorizations/issue")
    }

    override fun confirmBilling(command: ConfirmBillingCommand, billingKey: String): TossApiResponse {
        return sendRequest(command, tossPaymentConfig.testSecretKey, "$tossApiBaseUrl/billing/$billingKey")
    }

    override fun requestBrandpayAccessToken(command: CallbackAuthCommand): TossApiResponse {
        val requestData = mapOf(
            "grantType" to "AuthorizationCode",
            "customerKey" to command.customerKey,
            "code" to command.code
        )
        return sendRequest(requestData, tossPaymentConfig.testSecretKey, "$tossApiBaseUrl/brandpay/authorizations/access-token")
    }

    override fun confirmBrandpay(command: ConfirmBrandpayCommand): TossApiResponse {
        return sendRequest(command, tossPaymentConfig.testSecretKey, "$tossApiBaseUrl/brandpay/payments/confirm")
    }

    private fun sendRequest(requestData: Any, secretKey: String, urlString: String): TossApiResponse {
        val connection = createConnection(secretKey, urlString)
        return try {
            connection.outputStream.use { outputStream ->
                objectMapper.writeValue(outputStream, requestData)
            }

            val statusCode = connection.responseCode
            val responseStream: InputStream? = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            if (responseStream == null) {
                return TossApiResponse(statusCode, mapOf("error" to "Empty response"))
            }

            val responseBody: Map<String, Any> = responseStream.use { inputStream ->
                objectMapper.readValue(inputStream, object : TypeReference<Map<String, Any>>() {})
            }
            TossApiResponse(statusCode, responseBody)
        } finally {
            connection.disconnect()
        }
    }

    private fun createConnection(secretKey: String, urlString: String): HttpURLConnection {
        val url = URL(urlString)
        return (url.openConnection() as HttpURLConnection).apply {
            val auth = Base64.getEncoder().encodeToString("$secretKey:".toByteArray(StandardCharsets.UTF_8))
            setRequestProperty("Authorization", "Basic $auth")
            setRequestProperty("Content-Type", "application/json")
            requestMethod = "POST"
            doOutput = true
        }
    }

    private fun toConfirmedPaymentResult(response: TossApiResponse, memberId: Long?): ConfirmedPaymentResult {
        val body = response.body
        return ConfirmedPaymentResult(
            memberId = memberId ?: (body["memberId"]?.toString()?.toLong() ?: 0L),
            paymentKey = body["paymentKey"].toString(),
            orderId = body["orderId"].toString(),
            orderName = body["orderName"].toString(),
            method = body["method"].toString(),
            totalAmount = body["totalAmount"].toString().toInt(),
            status = PaymentStatus.from(body["status"]?.toString()),
            requestedAt = body["requestedAt"]?.toString(),
            approvedAt = body["approvedAt"]?.toString(),
            receiptUrl = extractReceiptUrl(body)
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractReceiptUrl(body: Map<String, Any>): String? {
        val receipt = body["receipt"] as? Map<String, Any> ?: return null
        return receipt["url"]?.toString()
    }
}
