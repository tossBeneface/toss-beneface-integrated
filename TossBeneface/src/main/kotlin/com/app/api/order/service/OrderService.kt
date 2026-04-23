package com.app.api.order.service

import com.app.api.order.dto.OrderRequest
import com.app.api.order.repository.OrderRepository
import com.app.domain.member.repository.MemberRepository
import com.app.domain.order.entity.OrderItem
import com.app.domain.order.entity.OrderPayment
import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import com.app.global.error.exception.EntityNotFoundException
import com.app.global.kafka.event.OrderCreatedEvent
import com.app.global.kafka.event.OrderItemPayload
import com.app.global.kafka.outbox.OutboxEvent
import com.app.global.kafka.outbox.OutboxRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderService(
    private val orderRepository: OrderRepository,
    private val memberRepository: MemberRepository,
    private val outboxRepository: OutboxRepository,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun createOrder(request: OrderRequest): Long {
        // 1. 회원 조회 (비관적 락으로 예산 보호)
        val member = memberRepository.findByIdWithPessimisticLock(request.memberId)
            .orElseThrow { EntityNotFoundException(ErrorCode.MEMBER_NOT_EXIST) }

        // 2. 예산 확인 및 차감 (DDD: 엔티티 내부 로직 활용)
        member.checkAndSubtractBudget(request.totalAmount)

        // 3. 주문 및 아이템 생성 (DDD: 팩토리 메서드 활용)
        val orderItems = request.items.map { itemReq ->
            OrderItem(
                name = itemReq.name,
                price = itemReq.price,
                count = itemReq.count
            )
        }
        val orderPayment = OrderPayment.createOrder(member, orderItems)

        // 4. 저장 (CascadeType.ALL에 의해 아이템도 자동 저장됨)
        val savedOrder = orderRepository.save(orderPayment)
        val orderId = savedOrder.id ?: throw IllegalStateException("Order save failed")

        val orderCreatedEvent = OrderCreatedEvent(
            orderId = orderId,
            cafeId = request.cafeId,
            cafeName = request.cafeName,
            memberId = member.memberId ?: throw IllegalStateException("Member id is missing"),
            totalAmount = savedOrder.totalAmount,
            items = savedOrder.items.map { item ->
                OrderItemPayload(
                    name = item.name,
                    price = item.price,
                    count = item.count
                )
            }
        )

        outboxRepository.save(
            OutboxEvent(
                aggregateId = orderId.toString(),
                aggregateType = "ORDER",
                eventType = "ORDER_CREATED",
                partitionKey = request.cafeId.toString(),
                payload = objectMapper.writeValueAsString(orderCreatedEvent)
            )
        )

        return orderId
    }

    @Transactional(readOnly = true)
    fun getMyOrders(memberId: Long): List<OrderPayment> {
        val member = memberRepository.findById(memberId)
            .orElseThrow { EntityNotFoundException(ErrorCode.MEMBER_NOT_EXIST) }
        
        return orderRepository.findByMember(member)
    }
}
