package com.app.auth.application.usecase

import com.app.auth.application.port.RefreshTokenStore
import com.app.auth.application.dto.TokenResponse
import com.app.auth.infra.security.JwtTokenProvider
import com.app.domain.member.service.MemberService
import com.app.global.error.ErrorCode
import com.app.global.error.exception.AuthenticationException
import com.app.global.jwt.service.TokenManager
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class IssueAccessTokenUseCase(
    private val memberService: MemberService,
    private val refreshTokenStore: RefreshTokenStore,
    private val tokenManager: TokenManager,
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun issue(refreshToken: String, response: HttpServletResponse): TokenResponse {
        // 1. refresh token 검증 및 claims 파싱
        val tokenClaims = jwtTokenProvider.parseRefreshToken(refreshToken)
        val memberIdFromToken = jwtTokenProvider.extractMemberId(tokenClaims)

        // 2. Redis에 저장된 refresh token 세션 검증
        val storedMemberId = refreshTokenStore.findMemberIdByToken(refreshToken)
            ?: throw AuthenticationException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)

        if (storedMemberId != memberIdFromToken) {
            throw AuthenticationException(ErrorCode.NOT_VALID_TOKEN)
        }

        // 3. 회원 정보 조회
        val member = memberService.findMemberById(storedMemberId)
        val memberId = member.memberId ?: throw IllegalArgumentException("Member id is missing")

        // 4. Token Manager를 통한 토큰 회전 (저장 및 쿠키 업데이트 포함)
        val jwtTokenDto = tokenManager.rotateToken(refreshToken, memberId, member.role, response)

        return jwtTokenDto.toTokenResponse(tokenManager.refreshTokenExpirationTime.toLong())
    }
}
