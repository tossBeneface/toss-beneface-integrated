package com.app.global.kafka.consumer

import com.app.api.addProduct.ProductRepository
import com.app.global.kafka.event.KafkaTopics
import com.app.global.kafka.event.OrderCreatedEvent
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.kafka.support.Acknowledgment
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class InventoryConsumer(
    private val objectMapper: ObjectMapper,
    private val productRepository: ProductRepository
) {

    private val log = LoggerFactory.getLogger(InventoryConsumer::class.java)

    @Transactional
    @KafkaListener(
        topics = [KafkaTopics.ORDER_CREATED],
        groupId = "inventory-service",
        containerFactory = "stringKafkaListenerContainerFactory"
    )
    fun consume(message: String, acknowledgment: Acknowledgment) {
        val event = objectMapper.readValue(message, OrderCreatedEvent::class.java)

        event.items.forEach { orderItem ->
            val product = productRepository.findByCafeAndMenuWithPessimisticLock(event.cafeName, orderItem.name)
                .orElseThrow {
                    IllegalStateException("Product not found for cafe=${event.cafeName}, menu=${orderItem.name}")
                }

            if (product.stock < orderItem.count) {
                throw IllegalStateException(
                    "Insufficient stock for cafe=${event.cafeName}, menu=${orderItem.name}, currentStock=${product.stock}"
                )
            }

            product.stock -= orderItem.count
        }

        log.info("Inventory updated for orderId={}, cafeName={}", event.orderId, event.cafeName)
        acknowledgment.acknowledge()
    }
}
