package com.app.e2e

import com.app.AbstractIntegrationTest
import com.app.api.order.dto.OrderItemRequest
import com.app.api.order.dto.OrderRequest
import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.repository.MemberRepository
import com.app.global.kafka.outbox.OutboxRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@AutoConfigureMockMvc
class TossBenefaceE2ETest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var outboxRepository: OutboxRepository

    private lateinit var savedMember: Member

    @BeforeEach
    fun setUp() {
        outboxRepository.deleteAll()
        memberRepository.deleteAll()

        val member = Member(
            email = "e2e@test.com",
            password = "password",
            memberName = "E2E테스터",
            phoneNumber = "010-9999-8888",
            gender = Gender.MALE,
            budget = 100000,
            role = Role.USER,
            memberStatus = MemberStatus.ACTIVATE
        )
        savedMember = memberRepository.save(member)
    }

    @Test
    @DisplayName("사용자가 주문 API를 호출하면 결제 처리되고 Outbox 이벤트가 생성된다 (E2E)")
    fun orderToOutboxE2ETest() {
        // given
        val request = OrderRequest(
            memberId = savedMember.memberId!!,
            cafeId = 1L,
            cafeName = "스타벅스",
            items = listOf(OrderItemRequest("아메리카노", 4500, 2)),
            totalAmount = 9000
        )

        // when & then
        mockMvc.perform(
            post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.orderId").exists())

        // DB 검증
        val updatedMember = memberRepository.findById(savedMember.memberId!!).get()
        assertEquals(91000, updatedMember.budget)

        val outboxEvents = outboxRepository.findAll()
        assertEquals(1, outboxEvents.size)
        assertEquals("ORDER_CREATED", outboxEvents[0].eventType)
    }
}
