package com.app.auth.application.dto

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
            grantType = requireNotNull(this.grantType) { "grantType is missing" },
            accessToken = requireNotNull(this.accessToken) { "accessToken is missing" },
            accessTokenExpireTime = requireNotNull(this.accessTokenExpireTime) { "accessTokenExpireTime is missing" },
            refreshToken = requireNotNull(this.refreshToken) { "refreshToken is missing" },
            refreshTokenExpirationTime = refreshTokenExpirationTime
        )
    }
}
