package com.app.global.kafka.consumer

import com.app.global.kafka.event.KafkaTopics
import com.app.global.kafka.event.VoiceCompletedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Component

@Component
class VoiceCompletedConsumer(
    private val objectMapper: ObjectMapper,
    private val messagingTemplate: SimpMessagingTemplate
) {

    private val log = LoggerFactory.getLogger(VoiceCompletedConsumer::class.java)

    @KafkaListener(
        topics = [KafkaTopics.VOICE_COMPLETED],
        groupId = "notification-service",
        containerFactory = "stringKafkaListenerContainerFactory"
    )
    fun consume(message: String, acknowledgment: Acknowledgment) {
        val event = objectMapper.readValue(message, VoiceCompletedEvent::class.java)

        messagingTemplate.convertAndSendToUser(
            event.memberId.toString(),
            "/queue/voice-result",
            event.result
        )

        log.info("Voice result pushed to websocket. requestId={}, memberId={}", event.requestId, event.memberId)
        acknowledgment.acknowledge()
    }
}
