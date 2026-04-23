package com.app.global.kafka.outbox

import com.app.global.kafka.event.KafkaTopics
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@ConditionalOnProperty(
    name = ["app.outbox.publisher.enabled"],
    havingValue = "true",
    matchIfMissing = true
)
class OutboxPublisher(
    private val outboxRepository: OutboxRepository,
    private val stringKafkaTemplate: KafkaTemplate<String, String>
) {

    private val log = LoggerFactory.getLogger(OutboxPublisher::class.java)

    @Scheduled(fixedDelay = 500)
    @Transactional
    fun publishUnpublishedEvents() {
        val unpublishedEvents = outboxRepository.findUnpublished()

        unpublishedEvents.forEach { event ->
            val topic = resolveTopic(event.eventType)

            try {
                stringKafkaTemplate.send(topic, event.partitionKey, event.payload).get()
                event.published = true
            } catch (exception: Exception) {
                log.error(
                    "Failed to publish outbox event. eventId={}, eventType={}, topic={}",
                    event.id,
                    event.eventType,
                    topic,
                    exception
                )

                if (exception is InterruptedException) {
                    Thread.currentThread().interrupt()
                }

                return
            }
        }
    }

    private fun resolveTopic(eventType: String): String {
        return when (eventType) {
            "ORDER_CREATED" -> KafkaTopics.ORDER_CREATED
            "ORDER_STATUS_UPDATED" -> KafkaTopics.ORDER_STATUS_UPDATED
            "PAYMENT_COMPLETED" -> KafkaTopics.PAYMENT_COMPLETED
            "PAYMENT_FAILED" -> KafkaTopics.PAYMENT_FAILED
            "VOICE_REQUESTED" -> KafkaTopics.VOICE_REQUESTED
            "VOICE_COMPLETED" -> KafkaTopics.VOICE_COMPLETED
            else -> throw IllegalArgumentException("Unsupported outbox event type: $eventType")
        }
    }
}
