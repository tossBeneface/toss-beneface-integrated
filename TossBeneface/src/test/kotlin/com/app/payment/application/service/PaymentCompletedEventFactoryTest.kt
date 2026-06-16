package com.app.payment.application.service

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.payment.entity.Payment
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PaymentCompletedEventFactoryTest {

    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())
    private val paymentCompletedEventFactory = PaymentCompletedEventFactory(objectMapper)

    @Test
    fun `creates payment completed outbox event`() {
        val payment = mockk<Payment>()
        every { payment.id } returns 100L
        every { payment.paymentKey } returns "payment-key"
        every { payment.totalAmount } returns 10_000
        every { payment.method } returns "card"
        every { payment.approvedAt } returns "2026-06-16T01:01:00+09:00"
        val member = createMember()

        val outboxEvent = paymentCompletedEventFactory.create(payment, member)
        val payload = objectMapper.readValue(outboxEvent.payload, object : TypeReference<Map<String, Any>>() {})

        assertEquals("100", outboxEvent.aggregateId)
        assertEquals("PAYMENT", outboxEvent.aggregateType)
        assertEquals("PAYMENT_COMPLETED", outboxEvent.eventType)
        assertEquals("1", outboxEvent.partitionKey)
        assertEquals(100, payload["paymentId"])
        assertEquals("payment-key", payload["paymentKey"])
        assertEquals(1, payload["memberId"])
        assertEquals("Member", payload["memberName"])
        assertEquals(10_000, payload["totalAmount"])
        assertEquals("card", payload["method"])
        assertEquals("2026-06-16T01:01:00+09:00", payload["approvedAt"])
    }

    private fun createMember() = Member(
        memberId = 1L,
        email = "member@example.com",
        password = "password",
        memberName = "Member",
        phoneNumber = "010-1234-5678",
        gender = Gender.MALE,
        budget = 10_000,
        role = Role.USER,
        memberStatus = MemberStatus.ACTIVATE
    )
}
