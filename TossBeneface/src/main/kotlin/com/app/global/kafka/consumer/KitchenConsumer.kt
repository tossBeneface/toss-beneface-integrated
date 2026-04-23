package com.app.global.kafka.consumer

import com.app.global.kafka.event.KafkaTopics
import com.app.global.kafka.event.OrderCreatedEvent
import com.app.global.kafka.event.OrderStatusUpdatedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class KitchenConsumer(
    private val objectMapper: ObjectMapper,
    private val kafkaTemplate: KafkaTemplate<String, Any>
) {

    private val log = LoggerFactory.getLogger(KitchenConsumer::class.java)

    @KafkaListener(
        topics = [KafkaTopics.ORDER_CREATED],
        groupId = "kitchen-service",
        containerFactory = "stringKafkaListenerContainerFactory"
    )
    fun consume(message: String, acknowledgment: Acknowledgment) {
        val event = objectMapper.readValue(message, OrderCreatedEvent::class.java)

        log.info(
            "Kitchen received order. orderId={}, cafeName={}, items={}",
            event.orderId,
            event.cafeName,
            event.items.map { "${it.name} x${it.count}" }
        )

        kafkaTemplate.send(
            KafkaTopics.ORDER_STATUS_UPDATED,
            event.cafeId.toString(),
            OrderStatusUpdatedEvent(
                orderId = event.orderId,
                cafeId = event.cafeId,
                memberId = event.memberId,
                status = "PREPARING"
            )
        ).get()

        acknowledgment.acknowledge()
    }
}
