package com.app.api.order.service

import com.app.AbstractIntegrationTest
import com.app.api.order.dto.OrderItemRequest
import com.app.api.order.dto.OrderRequest
import com.app.api.order.repository.OrderItemRepository
import com.app.api.order.repository.OrderRepository
import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.repository.MemberRepository
import com.app.global.kafka.outbox.OutboxRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired

class OrderIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var orderService: OrderService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var orderItemRepository: OrderItemRepository

    @Autowired
    private lateinit var outboxRepository: OutboxRepository

    private lateinit var savedMember: Member

    @BeforeEach
    fun setUp() {
        outboxRepository.deleteAll()
        orderItemRepository.deleteAll()
        orderRepository.deleteAll()
        memberRepository.deleteAll()

        val member = Member(
            email = "order@test.com",
            password = "password",
            memberName = "주문자",
            phoneNumber = "010-1111-2222",
            gender = Gender.FEMALE,
            budget = 50000,
            role = Role.USER,
            memberStatus = MemberStatus.ACTIVATE
        )
        savedMember = memberRepository.save(member)
    }

    @Test
    @DisplayName("주문 생성 시 주문, 주문 아이템, Outbox 이벤트가 모두 DB에 저장된다 (Cascade 확인)")
    fun createOrderIntegrationSuccess() {
        // given
        val totalAmount = 10000
        val request = createOrderRequest(savedMember.memberId!!, totalAmount)

        // when
        val orderId = orderService.createOrder(request)

        // then
        val updatedMember = memberRepository.findById(savedMember.memberId!!).get()
        assertEquals(40000, updatedMember.budget)

        val savedOrder = orderRepository.findById(orderId).get()
        assertEquals(totalAmount, savedOrder.totalAmount)
        assertEquals(2, savedOrder.items.size) // Cascade에 의해 아이템이 저장되어야 함

        val savedItems = orderItemRepository.findAll()
        assertEquals(2, savedItems.size)

        val outboxEvents = outboxRepository.findAll()
        assertEquals(1, outboxEvents.size)
        assertEquals("ORDER_CREATED", outboxEvents[0].eventType)
    }

    private fun createOrderRequest(memberId: Long, totalAmount: Int) = OrderRequest(
        memberId = memberId,
        cafeId = 100L,
        cafeName = "스타벅스 성수점",
        items = listOf(
            OrderItemRequest("아메리카노", 4500, 1),
            OrderItemRequest("카페라떼", 5500, 1)
        ),
        totalAmount = totalAmount
    )
}
