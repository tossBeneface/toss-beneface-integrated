package com.app.member.application.usecase

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.service.MemberService
import com.app.member.application.dto.ResolveSocialMemberCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Optional

class ResolveSocialMemberUseCaseTest {

    private val memberService = mockk<MemberService>()
    private val useCase = ResolveSocialMemberUseCase(memberService)

    @Test
    fun `returns existing social member`() {
        val member = createMember().apply {
            memberId = 7L
            socialType = "GOOGLE"
            socialId = "social-1"
        }
        every { memberService.findMemberBySocialIdentity("GOOGLE", "social-1") } returns Optional.of(member)

        val result = useCase.resolve(command())

        assertEquals(7L, result.memberId)
        assertEquals(Role.USER, result.role)
        verify(exactly = 0) { memberService.findMemberByEmail(any()) }
        verify(exactly = 0) { memberService.registerMember(any()) }
        verify(exactly = 0) { memberService.updateMember(any()) }
    }

    @Test
    fun `connects social identity to existing email member`() {
        val member = createMember().apply { memberId = 8L }
        every { memberService.findMemberBySocialIdentity("GOOGLE", "social-1") } returns Optional.empty()
        every { memberService.findMemberByEmail("member@example.com") } returns Optional.of(member)
        every { memberService.updateMember(member) } returns member

        val result = useCase.resolve(command())

        assertEquals(8L, result.memberId)
        assertEquals("GOOGLE", member.socialType)
        assertEquals("social-1", member.socialId)
        assertEquals("Social Member", member.memberName)
        verify(exactly = 1) { memberService.updateMember(member) }
    }

    @Test
    fun `registers new social member when no member exists`() {
        val capturedMember = slot<Member>()
        every { memberService.findMemberBySocialIdentity("GOOGLE", "social-1") } returns Optional.empty()
        every { memberService.findMemberByEmail("member@example.com") } returns Optional.empty()
        every { memberService.registerMember(capture(capturedMember)) } answers {
            capturedMember.captured.apply { memberId = 9L }
        }

        val result = useCase.resolve(command())

        assertEquals(9L, result.memberId)
        assertEquals("member@example.com", result.email)
        assertEquals("Social Member", result.memberName)
        assertEquals(Gender.UNKNOWN, result.gender)
        assertEquals(Role.USER, result.role)
        assertEquals(MemberStatus.ACTIVATE, result.memberStatus)
        assertEquals("GOOGLE", capturedMember.captured.socialType)
        assertEquals("social-1", capturedMember.captured.socialId)
    }

    private fun command(): ResolveSocialMemberCommand {
        return ResolveSocialMemberCommand(
            socialType = "GOOGLE",
            socialId = "social-1",
            email = "member@example.com",
            memberName = "Social Member"
        )
    }

    private fun createMember(): Member {
        return Member(
            email = "member@example.com",
            password = "encoded-password",
            memberName = "Existing Member",
            phoneNumber = "010-1234-5678",
            gender = Gender.MALE,
            profileImg = "",
            budget = 10_000_000,
            role = Role.USER,
            memberStatus = MemberStatus.ACTIVATE
        )
    }
}
