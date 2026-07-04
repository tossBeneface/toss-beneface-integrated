package com.app.payment.application.service

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.payment.entity.PaymentStatus
import com.app.payment.application.dto.ConfirmedPaymentResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test

class PaymentFactoryTest {

    private val paymentFactory = PaymentFactory()

    @Test
    fun `creates payment from confirmed payment result`() {
        val member = createMember()
        val confirmedPayment = ConfirmedPaymentResult(
            memberId = 1L,
            paymentKey = "payment-key",
            orderId = "order-id",
            orderName = "order-name",
            method = "card",
            totalAmount = 10_000,
            status = PaymentStatus.DONE,
            requestedAt = "2026-06-16T01:00:00+09:00",
            approvedAt = "2026-06-16T01:01:00+09:00",
            receiptUrl = "https://receipt.example"
        )

        val payment = paymentFactory.create(confirmedPayment, member)

        assertEquals("payment-key", payment.paymentKey)
        assertEquals("order-id", payment.orderId)
        assertEquals("order-name", payment.orderName)
        assertEquals("card", payment.method)
        assertEquals(10_000, payment.totalAmount)
        assertEquals(PaymentStatus.DONE, payment.status)
        assertEquals("2026-06-16T01:00:00+09:00", payment.requestedAt)
        assertEquals("2026-06-16T01:01:00+09:00", payment.approvedAt)
        assertEquals("https://receipt.example", payment.receiptUrl)
        assertSame(member, payment.member)
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
