package com.app.domain.member.repository

import com.app.AbstractIntegrationTest
import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional

@Transactional
class MemberRepositoryTest : AbstractIntegrationTest() {

    @Autowired
    lateinit var memberRepository: MemberRepository

    @Test
    @DisplayName("이메일로 회원 조회 테스트")
    fun findByEmailTest() {
        // given
        val email = "integration@test.com"
        val member = Member(
            email = email,
            password = "password",
            memberName = "Integ",
            phoneNumber = "010-0000-0000",
            gender = Gender.FEMALE,
            profileImg = "",
            budget = 5000,
            role = Role.USER,
            memberStatus = MemberStatus.ACTIVATE
        )
        memberRepository.save(member)

        // when
        val foundMember = memberRepository.findByEmail(email)

        // then
        assertTrue(foundMember.isPresent)
        assertEquals(email, foundMember.get().email)
    }

    @Test
    @DisplayName("잔액 업데이트 테스트")
    fun updateBudgetTest() {
        // given
        val member = Member(
            email = "budget@test.com",
            password = "password",
            memberName = "Budget",
            phoneNumber = "010-1111-1111",
            gender = Gender.MALE,
            profileImg = "",
            budget = 1000,
            role = Role.USER,
            memberStatus = MemberStatus.ACTIVATE
        )
        val savedMember = memberRepository.saveAndFlush(member)
        val memberId = savedMember.memberId!!

        // when
        val newBudget = 2000
        val updatedCount = memberRepository.updateBudget(memberId, newBudget)
        
        // then
        assertEquals(1, updatedCount)
        val foundMember = memberRepository.findById(memberId).get()
        assertEquals(newBudget, foundMember.budget)
    }
}
