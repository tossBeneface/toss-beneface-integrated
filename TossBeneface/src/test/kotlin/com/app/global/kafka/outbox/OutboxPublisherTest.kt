package com.app.global.kafka.outbox

import com.app.global.kafka.event.KafkaTopics
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.kafka.core.KafkaTemplate
import java.util.concurrent.CompletableFuture

class OutboxPublisherTest {

    private val outboxRepository = mockk<OutboxRepository>()
    private val kafkaTemplate = mockk<KafkaTemplate<String, String>>()

    private val outboxPublisher = OutboxPublisher(outboxRepository, kafkaTemplate)

    @Test
    @DisplayName("발행되지 않은 이벤트가 있을 경우 Kafka로 발행하고 published 상태를 true로 변경한다")
    fun publishUnpublishedEventsSuccess() {
        // given
        val event = OutboxEvent(
            aggregateId = "100",
            aggregateType = "ORDER",
            eventType = "ORDER_CREATED",
            partitionKey = "1",
            payload = "{\"orderId\": 100}"
        )
        
        every { outboxRepository.findUnpublished() } returns listOf(event)
        every { kafkaTemplate.send(any(), any(), any()) } returns CompletableFuture.completedFuture(mockk())

        // when
        outboxPublisher.publishUnpublishedEvents()

        // then
        verify { kafkaTemplate.send(KafkaTopics.ORDER_CREATED, "1", "{\"orderId\": 100}") }
        assertTrue(event.published)
    }

    @Test
    @DisplayName("Kafka 발행 중 예외가 발생하면 published 상태를 변경하지 않고 중단한다")
    fun publishUnpublishedEventsFailure() {
        // given
        val event = OutboxEvent(
            aggregateId = "100",
            aggregateType = "ORDER",
            eventType = "ORDER_CREATED",
            partitionKey = "1",
            payload = "{\"orderId\": 100}"
        )
        
        every { outboxRepository.findUnpublished() } returns listOf(event)
        every { kafkaTemplate.send(any(), any(), any()) } throws RuntimeException("Kafka error")

        // when
        outboxPublisher.publishUnpublishedEvents()

        // then
        verify { kafkaTemplate.send(KafkaTopics.ORDER_CREATED, "1", "{\"orderId\": 100}") }
        assertFalse(event.published)
    }

    // Assertions import for convenience
    private fun assertTrue(actual: Boolean) = org.junit.jupiter.api.Assertions.assertTrue(actual)
    private fun assertFalse(actual: Boolean) = org.junit.jupiter.api.Assertions.assertFalse(actual)
}
