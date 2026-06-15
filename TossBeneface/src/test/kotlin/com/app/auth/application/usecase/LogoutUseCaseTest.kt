package com.app.auth.application.usecase

import com.app.auth.infra.security.JwtTokenProvider
import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.service.MemberService
import com.app.global.jwt.service.TokenManager
import io.jsonwebtoken.Claims
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class LogoutUseCaseTest {

    @InjectMocks
    private lateinit var logoutUseCase: LogoutUseCase

    @Mock
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var tokenManager: TokenManager

    @Mock
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    @DisplayName("유효한 access token이면 memberId 기반으로 토큰을 무효화한다")
    fun logoutDestroysTokenByMemberId() {
        val accessToken = "valid-access-token"
        val claims = mock(Claims::class.java)
        val member = Member(
            email = "member@example.com",
            password = "encoded-password",
            memberName = "Existing User",
            phoneNumber = "010-9999-0000",
            gender = Gender.MALE,
            role = Role.USER,
            memberStatus = MemberStatus.ACTIVATE,
            profileImg = ""
        )
        member.memberId = 42L

        given(jwtTokenProvider.parseAccessToken(accessToken)).willReturn(claims)
        given(jwtTokenProvider.extractMemberId(claims)).willReturn(42L)
        given(memberService.findMemberById(42L)).willReturn(member)

        logoutUseCase.logout(accessToken)

        verify(tokenManager).destroyTokenByMemberId(42L)
    }
}
