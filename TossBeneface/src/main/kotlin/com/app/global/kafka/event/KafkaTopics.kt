package com.app.global.kafka.event

object KafkaTopics {
    const val ORDER_CREATED         = "order.created"
    const val ORDER_STATUS_UPDATED  = "order.status.updated"
    const val VOICE_REQUESTED       = "voice.requested"
    const val VOICE_COMPLETED       = "voice.completed"
    const val PAYMENT_COMPLETED     = "payment.completed"
    const val PAYMENT_FAILED        = "payment.failed"
}
