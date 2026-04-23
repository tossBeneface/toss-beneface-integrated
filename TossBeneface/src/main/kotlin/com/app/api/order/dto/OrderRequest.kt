package com.app.api.order.dto

data class OrderRequest(
    val memberId: Long,
    val cafeId: Long,
    val cafeName: String,
    val items: List<OrderItemRequest>,
    val totalAmount: Int
)

data class OrderItemRequest(
    val name: String,
    val price: Int,
    val count: Int
)
