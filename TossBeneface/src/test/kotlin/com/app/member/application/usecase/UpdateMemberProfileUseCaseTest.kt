package com.app.member.application.usecase

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.service.MemberService
import com.app.member.application.dto.UpdateMemberProfileCommand
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class UpdateMemberProfileUseCaseTest {

    private val memberService = mockk<MemberService>()
    private val useCase = UpdateMemberProfileUseCase(memberService)

    @Test
    fun `updates member profile`() {
        val member = createMember()
        every { memberService.findMemberById(42L) } returns member
        every { memberService.updateMember(any()) } answers { firstArg() }

        val result = useCase.update(
            UpdateMemberProfileCommand(
                memberId = 42L,
                memberName = "Updated Member",
                phoneNumber = "010-9999-0000",
                gender = "female",
                profileImg = null
            )
        )

        assertEquals(42L, result.memberId)
        assertEquals("Updated Member", result.memberName)
        assertEquals("010-9999-0000", result.phoneNumber)
        assertEquals(Gender.FEMALE, result.gender)
        assertNull(result.profileImg)
        assertEquals("Updated Member", member.memberName)
        assertEquals("010-9999-0000", member.phoneNumber)
        assertEquals(Gender.FEMALE, member.gender)
        assertNull(member.profileImg)
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
