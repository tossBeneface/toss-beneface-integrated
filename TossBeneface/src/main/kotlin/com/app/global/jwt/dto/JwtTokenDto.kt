package com.app.global.jwt.dto

import com.app.auth.application.dto.TokenResponse
import com.fasterxml.jackson.annotation.JsonFormat
import java.util.Date

data class JwtTokenDto(
    val memberId: String? = null,
    val grantType: String? = null,
    val accessToken: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val accessTokenExpireTime: Date? = null,
    val refreshToken: String? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val refreshTokenExpireTime: Date? = null
) {
    fun toTokenResponse(refreshTokenExpirationTime: Long): TokenResponse {
        return TokenResponse(
            grantType = this.grantType,
            accessToken = this.accessToken,
            accessTokenExpireTime = this.accessTokenExpireTime,
            refreshToken = this.refreshToken,
            refreshTokenExpirationTime = refreshTokenExpirationTime
        )
    }
}
