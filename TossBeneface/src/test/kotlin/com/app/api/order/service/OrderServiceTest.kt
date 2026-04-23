package com.app.api.order.service

import com.app.api.order.dto.OrderItemRequest
import com.app.api.order.dto.OrderRequest
import com.app.api.order.repository.OrderRepository
import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.repository.MemberRepository
import com.app.domain.order.entity.OrderPayment
import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import com.app.global.error.exception.EntityNotFoundException
import com.app.global.kafka.outbox.OutboxRepository
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.util.*

class OrderServiceTest {

    private val orderRepository = mockk<OrderRepository>()
    private val memberRepository = mockk<MemberRepository>()
    private val outboxRepository = mockk<OutboxRepository>(relaxed = true)
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    private val orderService = OrderService(
        orderRepository,
        memberRepository,
        outboxRepository,
        objectMapper
    )

    @Test
    @DisplayName("주문 성공 시 예산이 차감되고 주문 내역과 Outbox 이벤트가 저장된다")
    fun createOrderSuccess() {
        // given
        val memberId = 1L
        val initialBudget = 20000
        val totalAmount = 5000
        
        val member = createMember(memberId, initialBudget)
        val request = createOrderRequest(memberId, totalAmount)
        
        val orderPayment = mockk<OrderPayment>()
        every { orderPayment.id } returns 100L
        every { orderPayment.totalAmount } returns totalAmount
        every { orderPayment.items } returns mutableListOf()
        
        every { memberRepository.findByIdWithPessimisticLock(memberId) } returns Optional.of(member)
        every { orderRepository.save(any()) } returns orderPayment
        every { outboxRepository.save(any()) } answers { firstArg() }

        // when
        val resultOrderId = orderService.createOrder(request)

        // then
        assertEquals(100L, resultOrderId)
        assertEquals(initialBudget - totalAmount, member.budget)
        verify { orderRepository.save(any()) }
        verify { outboxRepository.save(any()) }
    }

    @Test
    @DisplayName("예산이 부족할 경우 BusinessException(INSUFFICIENT_BUDGET)이 발생한다")
    fun createOrderInsufficientBudget() {
        // given
        val memberId = 1L
        val initialBudget = 1000
        val totalAmount = 5000
        
        val member = createMember(memberId, initialBudget)
        val request = createOrderRequest(memberId, totalAmount)
        
        every { memberRepository.findByIdWithPessimisticLock(memberId) } returns Optional.of(member)

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            orderService.createOrder(request)
        }
        assertEquals(ErrorCode.INSUFFICIENT_BUDGET, exception.errorCode)
    }

    @Test
    @DisplayName("존재하지 않는 회원일 경우 EntityNotFoundException이 발생한다")
    fun createOrderMemberNotFound() {
        // given
        val memberId = 999L
        val request = createOrderRequest(memberId, 5000)
        
        every { memberRepository.findByIdWithPessimisticLock(memberId) } returns Optional.empty()

        // when & then
        val exception = assertThrows(EntityNotFoundException::class.java) {
            orderService.createOrder(request)
        }
        assertEquals(ErrorCode.MEMBER_NOT_EXIST, exception.errorCode)
    }

    private fun createMember(id: Long, budget: Int) = Member(
        memberId = id,
        email = "test@test.com",
        password = "password",
        memberName = "테스터",
        phoneNumber = "010-1234-5678",
        gender = Gender.MALE,
        budget = budget,
        role = Role.USER,
        memberStatus = MemberStatus.ACTIVATE
    )

    private fun createOrderRequest(memberId: Long, totalAmount: Int) = OrderRequest(
        memberId = memberId,
        cafeId = 1L,
        cafeName = "테스트 카페",
        items = listOf(
            OrderItemRequest("아메리카노", 3000, 1),
            OrderItemRequest("라떼", 2000, 1)
        ),
        totalAmount = totalAmount
    )
}
