package com.app.auth.application.usecase

import com.app.auth.application.port.RefreshTokenStore
import com.app.auth.infra.security.JwtTokenProvider
import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.service.MemberService
import com.app.global.error.ErrorCode
import com.app.global.error.exception.AuthenticationException
import com.app.global.jwt.dto.JwtTokenDto
import com.app.global.jwt.service.TokenManager
import io.jsonwebtoken.Claims
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import java.util.Date

@ExtendWith(MockitoExtension::class)
class IssueAccessTokenUseCaseTest {

    @InjectMocks
    private lateinit var issueAccessTokenUseCase: IssueAccessTokenUseCase

    @Mock
    private lateinit var memberService: MemberService

    @Mock
    private lateinit var refreshTokenStore: RefreshTokenStore

    @Mock
    private lateinit var tokenManager: TokenManager

    @Mock
    private lateinit var jwtTokenProvider: JwtTokenProvider

    @Test
    @DisplayName("유효한 리프레시 토큰이면 새 액세스 토큰 정보를 반환한다")
    fun issueReturnsNewTokenInfo() {
        val refreshToken = "valid-refresh-token"
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

        val accessTokenExpireTime = Date(System.currentTimeMillis() + 60_000)
        val jwtTokenDto = JwtTokenDto(
            memberId = "42",
            grantType = "Bearer",
            accessToken = "new-access-token",
            refreshToken = "new-refresh-token",
            accessTokenExpireTime = accessTokenExpireTime
        )

        given(jwtTokenProvider.parseRefreshToken(refreshToken)).willReturn(claims)
        given(jwtTokenProvider.extractMemberId(claims)).willReturn(42L)
        given(refreshTokenStore.findMemberIdByToken(refreshToken)).willReturn(42L)
        given(memberService.findMemberById(42L)).willReturn(member)
        given(tokenManager.rotateToken(refreshToken, 42L, Role.USER)).willReturn(jwtTokenDto)
        given(tokenManager.refreshTokenExpirationTime).willReturn("1209600000")

        val response = issueAccessTokenUseCase.issue(refreshToken)

        assertEquals("Bearer", response.grantType)
        assertEquals("new-access-token", response.accessToken)
        assertEquals(accessTokenExpireTime, response.accessTokenExpireTime)
        assertEquals("new-refresh-token", response.refreshToken)
        verify(tokenManager).rotateToken(refreshToken, 42L, Role.USER)
    }

    @Test
    @DisplayName("리프레시 토큰 검증 실패 시 예외를 발생시킨다")
    fun issueThrowsExceptionOnInvalidToken() {
        val refreshToken = "expired-refresh-token"

        val exception = AuthenticationException(ErrorCode.TOKEN_EXPIRED)
        org.mockito.Mockito.doThrow(exception).`when`(jwtTokenProvider).parseRefreshToken(refreshToken)

        val thrown = assertThrows(AuthenticationException::class.java) {
            issueAccessTokenUseCase.issue(refreshToken)
        }

        assertEquals(ErrorCode.TOKEN_EXPIRED, thrown.errorCode)
        verifyNoInteractions(tokenManager)
        verifyNoInteractions(refreshTokenStore)
    }
}
