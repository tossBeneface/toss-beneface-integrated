package com.app.auth.application.service

import com.app.auth.application.dto.JwtTokenDto
import com.app.auth.application.port.RefreshTokenStore
import com.app.auth.infra.security.JwtTokenProvider
import com.app.domain.member.constant.Role
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.Date

@Component
class TokenManager(
    private val refreshTokenStore: RefreshTokenStore,
    private val jwtTokenProvider: JwtTokenProvider,
    @Value("\${token.access-token-expiration-time}") val accessTokenExpirationTime: String,
    @Value("\${token.refresh-token-expiration-time}") val refreshTokenExpirationTime: String
) {
    fun createJwtTokenDto(memberId: Long, role: Role): JwtTokenDto {
        val accessTokenExpireTime = createAccessTokenExpireTime()
        val refreshTokenExpireTime = createRefreshTokenExpireTime()

        val accessToken = jwtTokenProvider.createAccessToken(memberId, role, accessTokenExpireTime)
        val refreshToken = jwtTokenProvider.createRefreshToken(memberId, refreshTokenExpireTime)

        refreshTokenStore.save(
            memberId,
            refreshToken,
            Duration.ofMillis(refreshTokenExpirationTime.toLong())
        )

        return JwtTokenDto(
            memberId = memberId.toString(),
            grantType = BEARER_GRANT_TYPE,
            accessToken = accessToken,
            accessTokenExpireTime = accessTokenExpireTime,
            refreshToken = refreshToken,
            refreshTokenExpireTime = refreshTokenExpireTime
        )
    }

    fun rotateToken(oldRefreshToken: String, memberId: Long, role: Role): JwtTokenDto {
        refreshTokenStore.delete(oldRefreshToken)
        return createJwtTokenDto(memberId, role)
    }

    fun destroyToken(refreshToken: String) {
        refreshTokenStore.delete(refreshToken)
    }

    fun destroyTokenByMemberId(memberId: Long) {
        refreshTokenStore.deleteByMemberId(memberId)
    }

    fun createAccessTokenExpireTime(): Date {
        return Date(System.currentTimeMillis() + accessTokenExpirationTime.toLong())
    }

    fun createRefreshTokenExpireTime(): Date {
        return Date(System.currentTimeMillis() + refreshTokenExpirationTime.toLong())
    }

    companion object {
        private const val BEARER_GRANT_TYPE = "Bearer"
    }
}
