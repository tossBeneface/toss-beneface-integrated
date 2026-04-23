package com.app.global.kafka.outbox

import com.app.AbstractIntegrationTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.kafka.test.context.EmbeddedKafka

class OutboxPublisherIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var outboxPublisher: OutboxPublisher

    @Autowired
    private lateinit var outboxRepository: OutboxRepository

    @BeforeEach
    fun setUp() {
        outboxRepository.deleteAll()
    }

    @Test
    @DisplayName("DB에 저장된 미발행 이벤트를 OutboxPublisher가 처리하여 발행 상태로 변경한다")
    fun publishUnpublishedEventsIntegrationSuccess() {
        // given
        val event = OutboxEvent(
            aggregateId = "1",
            aggregateType = "ORDER",
            eventType = "ORDER_CREATED",
            partitionKey = "1",
            payload = "{\"orderId\": 1}"
        )
        outboxRepository.save(event)

        // when
        outboxPublisher.publishUnpublishedEvents()

        // then
        val processedEvent = outboxRepository.findById(event.id!!).get()
        assertTrue(processedEvent.published)
        
        val unpublished = outboxRepository.findUnpublished()
        assertEquals(0, unpublished.size)
    }
}
