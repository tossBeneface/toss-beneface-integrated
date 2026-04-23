package com.app.api.addProduct

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
class FastApiVoiceService(
    private val restTemplate: RestTemplate,
    @Value("\${fast.api.voice-process-url}") private val fastApiVoiceProcessUrl: String
) {
    private val log = LoggerFactory.getLogger(FastApiVoiceService::class.java)

    @CircuitBreaker(name = "fastApiVoiceProcess", fallbackMethod = "fallbackProcessVoiceText")
    fun processVoiceText(recognizedText: String, brand: String, menuNames: List<String>): Map<String, Any>? {
        val fastApiRequest = mapOf(
            "text" to recognizedText,
            "brand" to brand,
            "menus" to menuNames
        )

        val fastApiResponse: ResponseEntity<Map<*, *>> = restTemplate.postForEntity(fastApiVoiceProcessUrl, fastApiRequest, Map::class.java)
        if (!fastApiResponse.statusCode.is2xxSuccessful || fastApiResponse.body == null) {
            throw IllegalStateException("FastAPI voice process call failed")
        }

        @Suppress("UNCHECKED_CAST")
        return fastApiResponse.body as Map<String, Any>?
    }

    @Suppress("unused")
    fun fallbackProcessVoiceText(
        recognizedText: String,
        brand: String,
        menuNames: List<String>,
        throwable: Throwable
    ): Map<String, Any> {
        log.warn("FastAPI voice process fallback activated for brand={}", brand, throwable)
        return mapOf(
            "message" to "음성 주문 추천 서비스를 일시적으로 사용할 수 없습니다.",
            "brand" to brand,
            "menus" to menuNames,
            "gpt_answer" to "",
            "fallback" to true
        )
    }
}
