package com.app.member.application.usecase

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.service.MemberService
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class GetMemberInfoUseCaseTest {

    @InjectMocks
    private lateinit var getMemberInfoUseCase: GetMemberInfoUseCase

    @Mock
    private lateinit var memberService: MemberService

    @Test
    @DisplayName("회원 정보를 application result로 반환한다")
    fun getMemberInfoReturnsResult() {
        val member = createMember()
        given(memberService.findMemberById(42L)).willReturn(member)

        val result = getMemberInfoUseCase.getMemberInfo(42L)

        assertEquals("42", result.memberId)
        assertEquals("member@example.com", result.email)
        assertEquals("Existing User", result.memberName)
        assertEquals("010-9999-0000", result.phoneNumber)
        assertEquals("MALE", result.gender)
        assertEquals(50_000, result.budget)
        assertEquals("profile.png", result.profileImg)
        assertEquals("USER", result.role)
    }

    @Test
    @DisplayName("회원 이름만 조회한다")
    fun getMemberNameReturnsName() {
        val member = createMember()
        given(memberService.findMemberById(42L)).willReturn(member)

        val memberName = getMemberInfoUseCase.getMemberName(42L)

        assertEquals("Existing User", memberName)
        verify(memberService).findMemberById(42L)
    }

    private fun createMember(): Member {
        val member = Member(
            email = "member@example.com",
            password = "encoded-password",
            memberName = "Existing User",
            phoneNumber = "010-9999-0000",
            gender = Gender.MALE,
            profileImg = "profile.png",
            budget = 50_000,
            role = Role.USER,
            memberStatus = MemberStatus.ACTIVATE
        )
        member.memberId = 42L
        return member
    }
}
