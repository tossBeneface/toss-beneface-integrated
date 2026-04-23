package com.app.domain.member.repository

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.test.context.ActiveProfiles

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class MemberRepositorySliceTest {

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Test
    @DisplayName("비관적 락을 사용하여 회원을 조회할 수 있다")
    fun findByIdWithPessimisticLockTest() {
        // given
        val member = Member(
            email = "repo@test.com",
            password = "password",
            memberName = "레포테스터",
            phoneNumber = "010-0000-0000",
            gender = Gender.MALE,
            budget = 1000,
            role = Role.USER,
            memberStatus = MemberStatus.ACTIVATE
        )
        val savedMember = memberRepository.save(member)
        memberRepository.flush()

        // when
        val foundMember = memberRepository.findByIdWithPessimisticLock(savedMember.memberId!!)

        // then
        assertTrue(foundMember.isPresent)
        assertEquals(savedMember.memberId, foundMember.get().memberId)
    }
}
