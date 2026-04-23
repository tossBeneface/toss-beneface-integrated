package com.app.global.kafka.consumer

import com.app.global.kafka.event.KafkaTopics
import com.app.global.kafka.event.OrderCreatedEvent
import com.app.global.kafka.event.PaymentCompletedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component

@Component
class AnalyticsConsumer(
    private val objectMapper: ObjectMapper,
    private val stringRedisTemplate: StringRedisTemplate
) {

    private val log = LoggerFactory.getLogger(AnalyticsConsumer::class.java)

    @KafkaListener(
        topics = [KafkaTopics.ORDER_CREATED],
        groupId = "analytics-service",
        containerFactory = "stringKafkaListenerContainerFactory"
    )
    fun consumeOrderCreated(message: String, acknowledgment: Acknowledgment) {
        val event = objectMapper.readValue(message, OrderCreatedEvent::class.java)
        val dateKey = event.createdAt.toLocalDate()

        stringRedisTemplate.opsForValue()
            .increment("analytics:daily:orders:$dateKey", 1)

        log.info("Analytics recorded order count. orderId={}, date={}", event.orderId, dateKey)
        acknowledgment.acknowledge()
    }

    @KafkaListener(
        topics = [KafkaTopics.PAYMENT_COMPLETED],
        groupId = "analytics-service",
        containerFactory = "stringKafkaListenerContainerFactory"
    )
    fun consumePaymentCompleted(message: String, acknowledgment: Acknowledgment) {
        val event = objectMapper.readValue(message, PaymentCompletedEvent::class.java)
        val dateKey = event.completedAt.toLocalDate()

        stringRedisTemplate.opsForValue()
            .increment("analytics:daily:sales:$dateKey", event.totalAmount.toLong())

        log.info(
            "Analytics recorded daily sales. paymentId={}, memberId={}, amount={}, date={}",
            event.paymentId,
            event.memberId,
            event.totalAmount,
            dateKey
        )
        acknowledgment.acknowledge()
    }
}
