package com.app.domain.member.service

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.repository.MemberRepository
import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import java.util.*

@ExtendWith(MockKExtension::class)
class MemberServiceTest {

    @MockK
    lateinit var memberRepository: MemberRepository

    @InjectMockKs
    lateinit var memberService: MemberService

    @Test
    @DisplayName("회원가입 성공 테스트")
    fun registerMemberSuccess() {
        // given
        val member = createMember("test@example.com")
        every { memberRepository.findByEmail(any()) } returns Optional.empty()
        every { memberRepository.save(any()) } returns member

        // when
        val result = memberService.registerMember(member)

        // then
        assertEquals(member.email, result.email)
        verify(exactly = 1) { memberRepository.save(any()) }
    }

    @Test
    @DisplayName("회원가입 중복 이메일 예외 테스트")
    fun registerMemberDuplicateEmail() {
        // given
        val member = createMember("test@example.com")
        every { memberRepository.findByEmail(any()) } returns Optional.of(member)

        // when & then
        val exception = assertThrows<BusinessException> {
            memberService.registerMember(member)
        }
        assertEquals(ErrorCode.ALREADY_REGISTERED_MEMBER, exception.errorCode)
    }

    private fun createMember(email: String): Member {
        return Member(
            email = email,
            password = "password",
            memberName = "Tester",
            phoneNumber = "010-1234-5678",
            gender = Gender.MALE,
            profileImg = "",
            budget = 1000,
            role = Role.USER,
            memberStatus = MemberStatus.ACTIVATE
        )
    }
}
