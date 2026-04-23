package com.app.global.jwt.service

import com.app.auth.application.port.RefreshTokenStore
import com.app.auth.infra.security.JwtTokenProvider
import com.app.auth.infra.web.RefreshTokenCookieManager
import com.app.domain.member.constant.Role
import com.app.global.jwt.dto.JwtTokenDto
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.Date

@Component
class TokenManager(
    private val refreshTokenStore: RefreshTokenStore,
    private val refreshTokenCookieManager: RefreshTokenCookieManager,
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${token.access-token-expiration-time}") val accessTokenExpirationTime: String,
    @Value("\${token.refresh-token-expiration-time}") val refreshTokenExpirationTime: String
) {
    /**
     * Access/Refresh 토큰 세트를 생성하고, Refresh 토큰을 저장소(Redis 등)에 저장하며 쿠키를 설정합니다.
     */
    fun createJwtTokenDto(memberId: Long, role: Role, response: HttpServletResponse): JwtTokenDto {
        val accessTokenExpireTime = createAccessTokenExpireTime()
        val refreshTokenExpireTime = createRefreshTokenExpireTime()

        val accessToken = jwtTokenProvider.createAccessToken(memberId, role, accessTokenExpireTime)
        val refreshToken = jwtTokenProvider.createRefreshToken(memberId, refreshTokenExpireTime)

        // 1. 저장소에 Refresh Token 저장
        refreshTokenStore.save(
            memberId,
            refreshToken,
            Duration.ofMillis(refreshTokenExpirationTime.toLong())
        )

        // 2. HTTP Only 쿠키 설정 (암호화 포함)
        refreshTokenCookieManager.addRefreshTokenCookie(response, refreshToken, refreshTokenExpirationTime.toLong())

        return JwtTokenDto(
            memberId = memberId.toString(),
            grantType = "Bearer",
            accessToken = accessToken,
            accessTokenExpireTime = accessTokenExpireTime,
            refreshToken = refreshToken,
            refreshTokenExpireTime = refreshTokenExpireTime
        )
    }

    /**
     * 기존 Refresh Token을 무효화하고 새로운 토큰 세트를 발급합니다. (Token Rotation)
     */
    fun rotateToken(oldRefreshToken: String, memberId: Long, role: Role, response: HttpServletResponse): JwtTokenDto {
        // 기존 토큰 삭제
        refreshTokenStore.delete(oldRefreshToken)

        // 새 토큰 생성 및 저장, 쿠키 설정
        return createJwtTokenDto(memberId, role, response)
    }

    /**
     * 로그아웃 처리를 위해 쿠키를 삭제하고 저장소의 토큰을 무효화합니다.
     */
    fun destroyToken(response: HttpServletResponse, refreshToken: String) {
        refreshTokenStore.delete(refreshToken)
        refreshTokenCookieManager.removeRefreshTokenCookie(response)
    }

    /**
     * 회원 ID를 기반으로 모든 기기의 토큰을 무효화하고 쿠키를 삭제합니다. (로그아웃 시 사용)
     */
    fun destroyTokenByMemberId(response: HttpServletResponse, memberId: Long) {
        refreshTokenStore.deleteByMemberId(memberId)
        refreshTokenCookieManager.removeRefreshTokenCookie(response)
    }

    fun createAccessTokenExpireTime(): Date {
        return Date(System.currentTimeMillis() + accessTokenExpirationTime.toLong())
    }

    fun createRefreshTokenExpireTime(): Date {
        return Date(System.currentTimeMillis() + refreshTokenExpirationTime.toLong())
    }
}
