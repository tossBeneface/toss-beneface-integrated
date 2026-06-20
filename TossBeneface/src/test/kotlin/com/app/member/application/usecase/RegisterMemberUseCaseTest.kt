package com.app.member.application.usecase

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.service.MemberService
import com.app.member.application.dto.RegisterMemberAuthority
import com.app.member.application.dto.RegisterMemberCommand
import com.app.member.application.dto.RegisterMemberIdentity
import com.app.member.application.dto.RegisterMemberProfile
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RegisterMemberUseCaseTest {

    private val memberService = mockk<MemberService>()
    private val useCase = RegisterMemberUseCase(memberService)

    @Test
    fun `registers local member from separated identity profile authority and initial state`() {
        val capturedMember = slot<Member>()
        every { memberService.registerMember(capture(capturedMember)) } answers {
            capturedMember.captured.apply { memberId = 42L }
        }

        val result = useCase.register(
            RegisterMemberCommand(
                identity = RegisterMemberIdentity(
                    email = "member@example.com",
                    encodedPassword = "encoded-password"
                ),
                profile = RegisterMemberProfile(
                    memberName = "Member",
                    phoneNumber = "010-1234-5678",
                    gender = "male",
                    profileImg = null
                ),
                authority = RegisterMemberAuthority(role = "user")
            )
        )

        assertEquals(42L, result.memberId)
        assertEquals("member@example.com", result.email)
        assertEquals("Member", result.memberName)
        assertEquals(Gender.MALE, result.gender)
        assertEquals(Role.USER, result.role)
        assertEquals(MemberStatus.ACTIVATE, result.memberStatus)
        assertEquals(10_000_000, result.budget)

        val member = capturedMember.captured
        assertEquals("encoded-password", member.password)
        assertEquals("", member.profileImg)
        verify(exactly = 1) { memberService.registerMember(any()) }
    }
}
