package com.app.domain.payment.entity

import com.app.domain.member.entity.Member
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "payment")
class Payment(
    @Column(nullable = false)
    val paymentKey: String,

    @Column(nullable = false)
    val orderId: String,

    @Column(nullable = false)
    val orderName: String,

    @Column(nullable = false)
    val method: String,

    @Column(nullable = false)
    val totalAmount: Int,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus = PaymentStatus.READY,

    val requestedAt: String? = null,
    val approvedAt: String? = null,
    val receiptUrl: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    val member: Member? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    companion object {
        // 기존 Java/Builder 패턴 호환을 위한 생성 메서드
        @JvmStatic
        fun builder() = Builder()
    }

    class Builder {
        private var paymentKey: String = ""
        private var orderId: String = ""
        private var orderName: String = ""
        private var method: String = ""
        private var totalAmount: Int = 0
        private var status: String = "READY"
        private var requestedAt: String? = null
        private var approvedAt: String? = null
        private var receiptUrl: String? = null
        private var member: Member? = null

        fun paymentKey(paymentKey: String) = apply { this.paymentKey = paymentKey }
        fun orderId(orderId: String) = apply { this.orderId = orderId }
        fun orderName(orderName: String) = apply { this.orderName = orderName }
        fun method(method: String) = apply { this.method = method }
        fun totalAmount(totalAmount: Int) = apply { this.totalAmount = totalAmount }
        fun status(status: String) = apply { this.status = status }
        fun requestedAt(requestedAt: String?) = apply { this.requestedAt = requestedAt }
        fun approvedAt(approvedAt: String?) = apply { this.approvedAt = approvedAt }
        fun receiptUrl(receiptUrl: String?) = apply { this.receiptUrl = receiptUrl }
        fun member(member: Member?) = apply { this.member = member }

        fun build() = Payment(
            paymentKey = paymentKey,
            orderId = orderId,
            orderName = orderName,
            method = method,
            totalAmount = totalAmount,
            status = PaymentStatus.from(status),
            requestedAt = requestedAt,
            approvedAt = approvedAt,
            receiptUrl = receiptUrl,
            member = member
        )
    }
}
