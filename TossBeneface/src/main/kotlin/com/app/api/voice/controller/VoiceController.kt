package com.app.api.voice.controller

import com.app.api.addProduct.ProductService
import com.app.api.voice.dto.VoiceProcessRequest
import com.app.auth.infra.security.JwtTokenProvider
import com.app.auth.infra.web.BearerTokenResolver
import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import com.app.global.kafka.event.KafkaTopics
import com.app.global.kafka.event.VoiceRequestedEvent
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/products")
class VoiceController(
    private val productService: ProductService,
    private val kafkaTemplate: KafkaTemplate<String, Any>,
    private val bearerTokenResolver: BearerTokenResolver,
    private val jwtTokenProvider: JwtTokenProvider
) {

    private val log = LoggerFactory.getLogger(VoiceController::class.java)

    @PostMapping("/voice-process")
    fun processVoiceText(
        @RequestBody request: VoiceProcessRequest,
        httpServletRequest: HttpServletRequest
    ): ResponseEntity<Map<String, Any>> {
        if (request.text.isBlank()) {
            throw BusinessException(ErrorCode.EMPTY_REQUEST_TEXT)
        }
        if (request.brand.isBlank()) {
            throw BusinessException(ErrorCode.EMPTY_BRAND)
        }

        val memberId = resolveMemberId(request, httpServletRequest)
        val requestId = UUID.randomUUID().toString()
        val menuNames = productService.getProductsByCafe(request.brand).mapNotNull { it.menu }

        val event = VoiceRequestedEvent(
            requestId = requestId,
            memberId = memberId,
            audioUrl = request.audioUrl,
            text = request.text,
            brand = request.brand,
            menus = menuNames
        )

        kafkaTemplate.send(KafkaTopics.VOICE_REQUESTED, memberId.toString(), event)
            .whenComplete { _, exception ->
                if (exception != null) {
                    log.error("Failed to enqueue voice request. requestId={}, memberId={}", requestId, memberId, exception)
                } else {
                    log.info("Voice request enqueued. requestId={}, memberId={}", requestId, memberId)
                }
            }

        return ResponseEntity.accepted().body(
            mapOf(
                "requestId" to requestId,
                "message" to "음성 주문 요청이 접수되었습니다."
            )
        )
    }

    private fun resolveMemberId(request: VoiceProcessRequest, httpServletRequest: HttpServletRequest): Long {
        request.memberId?.let { return it }

        val authorizationHeader = httpServletRequest.getHeader("Authorization")
            ?: throw BusinessException(ErrorCode.INVALID_INPUT_VALUE)

        val token = bearerTokenResolver.resolve(authorizationHeader)
        val claims = jwtTokenProvider.parseAccessToken(token)
        return jwtTokenProvider.extractMemberId(claims)
    }
}
