package com.app.global.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration

@Configuration
class TossPaymentConfig(
    @Value("\${payment.toss.test_client_api_key}")
    val testClientApiKey: String,

    @Value("\${payment.toss.test_secret_api_key}")
    val testSecretKey: String,

    @Value("\${payment.toss.success_url}")
    val successUrl: String,

    @Value("\${payment.toss.fail_url}")
    val failUrl: String
) {
    companion object {
        const val URL = "https://api.tosspayments.com/v1/payments/"
    }
}
