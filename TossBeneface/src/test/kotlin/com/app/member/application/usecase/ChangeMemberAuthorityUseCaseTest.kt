package com.app.member.application.usecase

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.service.MemberService
import com.app.member.application.dto.ChangeMemberAuthorityCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class ChangeMemberAuthorityUseCaseTest {

    private val memberService = mockk<MemberService>()
    private val useCase = ChangeMemberAuthorityUseCase(memberService)

    @Test
    fun `changes member authority`() {
        val member = createMember()
        every { memberService.findMemberById(42L) } returns member
        every { memberService.updateMember(any()) } answers { firstArg() }

        val result = useCase.change(
            ChangeMemberAuthorityCommand(
                memberId = 42L,
                role = "admin"
            )
        )

        assertEquals(42L, result.memberId)
        assertEquals(Role.ADMIN, result.role)
        assertEquals(Role.ADMIN, member.role)
        verify(exactly = 1) { memberService.updateMember(member) }
    }

    private fun createMember() = Member(
        memberId = 42L,
        email = "member@example.com",
        password = "password",
        memberName = "Member",
        phoneNumber = "010-1234-5678",
        gender = Gender.MALE,
        profileImg = "profile.png",
        budget = 10_000,
        role = Role.USER,
        memberStatus = MemberStatus.ACTIVATE
    )
}
