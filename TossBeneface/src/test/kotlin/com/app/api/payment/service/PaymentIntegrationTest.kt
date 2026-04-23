package com.app.api.payment.service

import com.app.AbstractIntegrationTest
import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.repository.MemberRepository
import com.app.domain.payment.entity.PaymentStatus
import com.app.domain.payment.repository.PaymentRepository
import com.app.global.kafka.outbox.OutboxRepository
import com.app.payment.application.dto.ConfirmedPaymentResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.time.OffsetDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors

class PaymentIntegrationTest : AbstractIntegrationTest() {

    @Autowired
    private lateinit var paymentService: PaymentService

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var paymentRepository: PaymentRepository

    @Autowired
    private lateinit var outboxRepository: OutboxRepository

    private lateinit var savedMember: Member

    @BeforeEach
    fun setUp() {
        outboxRepository.deleteAll()
        paymentRepository.deleteAll()
        memberRepository.deleteAll()

        val member = Member(
            email = "test@test.com",
            password = "password",
            memberName = "테스터",
            phoneNumber = "010-1234-5678",
            gender = Gender.MALE,
            budget = 10000,
            role = Role.USER,
            memberStatus = MemberStatus.ACTIVATE
        )
        savedMember = memberRepository.save(member)
    }

    @Test
    @DisplayName("결제 성공 시 DB에 결제 내역과 Outbox 이벤트가 한 트랜잭션으로 저장된다")
    fun savePaymentIntegrationSuccess() {
        // given
        val paymentAmount = 3000
        val confirmedPayment = createConfirmedPayment(savedMember.memberId!!, paymentAmount)

        // when
        paymentService.savePayment(confirmedPayment)

        // then
        val updatedMember = memberRepository.findById(savedMember.memberId!!).get()
        assertEquals(7000, updatedMember.budget)

        val payments = paymentRepository.findAll()
        assertEquals(1, payments.size)
        assertEquals(paymentAmount, payments[0].totalAmount)

        val outboxEvents = outboxRepository.findAll()
        assertEquals(1, outboxEvents.size)
        assertEquals("PAYMENT_COMPLETED", outboxEvents[0].eventType)
    }

    @Test
    @DisplayName("비관적 락 테스트: 동시에 5개의 결제 요청이 와도 예산이 정확히 차감된다")
    fun pessimisticLockConcurrencyTest() {
        // given
        val threadCount = 5
        val paymentAmount = 1000
        val executorService = Executors.newFixedThreadPool(threadCount)
        val latch = CountDownLatch(threadCount)

        // when
        repeat(threadCount) {
            executorService.submit {
                try {
                    val confirmedPayment = createConfirmedPayment(savedMember.memberId!!, paymentAmount)
                    paymentService.savePayment(confirmedPayment)
                } finally {
                    latch.countDown()
                }
            }
        }
        latch.await()

        // then
        val updatedMember = memberRepository.findById(savedMember.memberId!!).get()
        assertEquals(5000, updatedMember.budget) // 10000 - (1000 * 5)
        
        val payments = paymentRepository.findAll()
        assertEquals(5, payments.size)
    }

    private fun createConfirmedPayment(memberId: Long, amount: Int) = ConfirmedPaymentResult(
        paymentKey = "key_" + System.nanoTime(),
        orderId = "order_" + System.nanoTime(),
        orderName = "테스트 상품",
        method = "카드",
        totalAmount = amount,
        status = PaymentStatus.DONE,
        requestedAt = OffsetDateTime.now().toString(),
        approvedAt = OffsetDateTime.now().toString(),
        receiptUrl = "http://receipt.url",
        memberId = memberId
    )
}
