package com.app.domain.order.entity

import com.app.domain.member.entity.Member
import jakarta.persistence.*

@Entity
@Table(name = "order_payment")
class OrderPayment(
    @Column(nullable = false)
    var totalAmount: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null,

    val cardReason: String? = null,

    @OneToMany(mappedBy = "orderPayment", cascade = [CascadeType.ALL], orphanRemoval = true)
    val items: MutableList<OrderItem> = mutableListOf()
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    /**
     * 주문 항목 추가 편의 메서드
     */
    fun addItem(item: OrderItem) {
        items.add(item)
        item.orderPayment = this
    }

    /**
     * 총 주문 금액 계산
     */
    fun calculateTotalAmount() {
        this.totalAmount = items.sumOf { it.price * it.count }
    }

    companion object {
        fun createOrder(member: Member, items: List<OrderItem>): OrderPayment {
            val orderPayment = OrderPayment(member = member)
            items.forEach { orderPayment.addItem(it) }
            orderPayment.calculateTotalAmount()
            return orderPayment
        }
    }
}
