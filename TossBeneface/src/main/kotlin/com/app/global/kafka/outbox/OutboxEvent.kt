package com.app.global.kafka.outbox

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "outbox_event",
    indexes = [Index(name = "idx_outbox_published_created", columnList = "published, created_at")]
)
class OutboxEvent(
    @Column(name = "aggregate_id", nullable = false)
    val aggregateId: String,        // 도메인 ID (주문 ID, 결제 key 등)

    @Column(name = "aggregate_type", nullable = false, length = 50)
    val aggregateType: String,      // "ORDER" | "PAYMENT" | "VOICE"

    @Column(name = "event_type", nullable = false, length = 50)
    val eventType: String,          // "ORDER_CREATED" | "PAYMENT_COMPLETED" 등

    @Column(name = "partition_key", nullable = false)
    val partitionKey: String,       // Kafka 파티션 키 (cafe_id, member_id 등)

    @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
    val payload: String,            // JSON 직렬화된 이벤트 본문

    @Column(name = "published", nullable = false)
    var published: Boolean = false,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null
}
