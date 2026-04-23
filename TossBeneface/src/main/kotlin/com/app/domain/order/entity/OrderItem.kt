package com.app.domain.order.entity

import jakarta.persistence.*

@Entity
@Table(name = "order_item")
class OrderItem(
    @Column(nullable = false)
    val name: String,

    @Column(nullable = false)
    val price: Int,

    @Column(nullable = false)
    val count: Int,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_payment_id")
    var orderPayment: OrderPayment? = null
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}
