package com.app.api.order.controller

import com.app.api.order.dto.OrderRequest
import com.app.api.order.service.OrderService
import com.app.global.resolver.memberInfo.MemberInfo
import com.app.global.resolver.memberInfo.MemberInfoDto
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/orders")
class OrderController(
    private val orderService: OrderService
) {
    private val log = LoggerFactory.getLogger(OrderController::class.java)

    @PostMapping
    fun createOrder(@RequestBody request: OrderRequest): ResponseEntity<Map<String, Any>> {
        log.debug("Received order: {}", request)
        
        val orderId = orderService.createOrder(request)

        return ResponseEntity.ok(mapOf(
            "orderId" to orderId,
            "message" to "주문이 성공적으로 접수되었습니다."
        ))
    }

    @GetMapping("/order-list")
    fun getMyOrders(@MemberInfo memberInfoDto: MemberInfoDto): ResponseEntity<Map<String, Any>> {
        log.debug("Get orders for memberId: {}", memberInfoDto.memberId)

        val orders = orderService.getMyOrders(memberInfoDto.memberId)

        val orderList = orders.map { order ->
            mapOf(
                "orderId" to (order.id ?: 0L),
                "totalAmount" to order.totalAmount,
                "items" to order.items.map { item ->
                    mapOf(
                        "name" to item.name,
                        "price" to item.price,
                        "count" to item.count
                    )
                }
            )
        }

        return ResponseEntity.ok(mapOf("orders" to orderList))
    }
}
