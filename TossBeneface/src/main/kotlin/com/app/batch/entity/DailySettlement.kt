package com.app.batch.entity

import jakarta.persistence.*
import java.time.LocalDate
import java.time.LocalDateTime

@Entity
@Table(
    name = "daily_settlement",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_daily_settlement_target_payment",
            columnNames = ["target_date", "payment_id"]
        )
    ]
)
class DailySettlement(
    @Column(name = "target_date", nullable = false)
    val targetDate: LocalDate,

    @Column(name = "payment_id", nullable = false)
    val paymentId: Long,

    @Column(name = "payment_key", nullable = false)
    val paymentKey: String,

    @Column(name = "order_id", nullable = false)
    val orderId: String,

    @Column(name = "member_id", nullable = false)
    val memberId: Long,

    @Column(name = "total_amount", nullable = false)
    val totalAmount: Int,

    @Column(name = "approved_at")
    val approvedAt: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    val id: Long? = null
}
