package com.app.api.payment.service

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.repository.MemberRepository
import com.app.domain.payment.entity.Payment
import com.app.domain.payment.entity.PaymentStatus
import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import com.app.global.error.exception.EntityNotFoundException
import com.app.global.kafka.outbox.OutboxRepository
import com.app.payment.application.dto.ConfirmedPaymentResult
import com.app.payment.application.port.PaymentStore
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import java.util.*

class PaymentServiceTest {

    private val paymentStore = mockk<PaymentStore>()
    private val memberRepository = mockk<MemberRepository>()
    private val outboxRepository = mockk<OutboxRepository>(relaxed = true)
    private val objectMapper = ObjectMapper().registerModule(JavaTimeModule())

    private val paymentService = PaymentService(
        paymentStore,
        memberRepository,
        outboxRepository,
        objectMapper
    )

    @Test
    @DisplayName("결제 성공 시 잔액이 차감되고 결제 내역과 Outbox 이벤트가 저장된다")
    fun savePaymentSuccess() {
        // given
        val memberId = 1L
        val initialBudget = 10000
        val paymentAmount = 3000
        
        val member = createMember(memberId, initialBudget)
        val confirmedPayment = createConfirmedPayment(memberId, paymentAmount)
        
        val payment = mockk<Payment>()
        every { payment.id } returns 100L
        every { payment.paymentKey } returns confirmedPayment.paymentKey
        every { payment.totalAmount } returns paymentAmount
        every { payment.method } returns confirmedPayment.method
        every { payment.approvedAt } returns confirmedPayment.approvedAt
        
        every { memberRepository.findByIdWithPessimisticLock(memberId) } returns Optional.of(member)
        every { paymentStore.save(any()) } returns payment
        every { outboxRepository.save(any()) } answers { firstArg() }

        // when
        paymentService.savePayment(confirmedPayment)

        // then
        assertEquals(initialBudget - paymentAmount, member.budget)
        verify { paymentStore.save(any()) }
        verify { outboxRepository.save(any()) }
    }

    @Test
    @DisplayName("잔액이 부족할 경우 BusinessException(INSUFFICIENT_BUDGET)이 발생한다")
    fun savePaymentInsufficientBudget() {
        // given
        val memberId = 1L
        val initialBudget = 1000
        val paymentAmount = 3000
        
        val member = createMember(memberId, initialBudget)
        val confirmedPayment = createConfirmedPayment(memberId, paymentAmount)
        
        val payment = mockk<Payment>()
        every { payment.totalAmount } returns paymentAmount
        
        every { memberRepository.findByIdWithPessimisticLock(memberId) } returns Optional.of(member)
        every { paymentStore.save(any()) } returns payment

        // when & then
        val exception = assertThrows(BusinessException::class.java) {
            paymentService.savePayment(confirmedPayment)
        }
        assertEquals(ErrorCode.INSUFFICIENT_BUDGET, exception.errorCode)
    }

    @Test
    @DisplayName("존재하지 않는 회원일 경우 EntityNotFoundException이 발생한다")
    fun savePaymentMemberNotFound() {
        // given
        val memberId = 999L
        val confirmedPayment = createConfirmedPayment(memberId, 1000)
        
        every { memberRepository.findByIdWithPessimisticLock(memberId) } returns Optional.empty()

        // when & then
        val exception = assertThrows(EntityNotFoundException::class.java) {
            paymentService.savePayment(confirmedPayment)
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

    private fun createConfirmedPayment(memberId: Long, amount: Int) = ConfirmedPaymentResult(
        paymentKey = "paymentKey",
        orderId = "orderId",
        orderName = "orderName",
        method = "카드",
        totalAmount = amount,
        status = PaymentStatus.DONE,
        requestedAt = OffsetDateTime.now().toString(),
        approvedAt = OffsetDateTime.now().toString(),
        receiptUrl = "http://receipt.url",
        memberId = memberId
    )
}
