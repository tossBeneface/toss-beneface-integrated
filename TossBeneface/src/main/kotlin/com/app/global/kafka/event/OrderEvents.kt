package com.app.global.kafka.event

import java.time.LocalDateTime

data class OrderCreatedEvent(
    val orderId: Long,
    val cafeId: Long,           // Partition Key — 같은 카페 주문은 같은 Partition
    val cafeName: String,
    val memberId: Long,
    val totalAmount: Int,
    val items: List<OrderItemPayload>,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

data class OrderItemPayload(
    val name: String,
    val price: Int,
    val count: Int
)

data class OrderStatusUpdatedEvent(
    val orderId: Long,
    val cafeId: Long,
    val memberId: Long,
    val status: String,         // PREPARING | READY | COMPLETED
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
