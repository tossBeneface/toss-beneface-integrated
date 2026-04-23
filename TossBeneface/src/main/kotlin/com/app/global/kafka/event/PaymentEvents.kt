package com.app.global.kafka.event

import java.time.LocalDateTime

data class PaymentCompletedEvent(
    val paymentId: Long,
    val paymentKey: String,
    val memberId: Long,
    val memberName: String,
    val totalAmount: Int,
    val method: String,
    val approvedAt: String?,
    val completedAt: LocalDateTime = LocalDateTime.now()
)

data class PaymentFailedEvent(
    val orderId: String,
    val memberId: Long,
    val reason: String,
    val failedAt: LocalDateTime = LocalDateTime.now()
)
