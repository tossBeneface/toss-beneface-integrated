package com.app.auth.application.dto

import java.util.Date

data class TokenResponse(
    val grantType: String,
    val accessToken: String,
    val accessTokenExpireTime: Date,
    val refreshToken: String,
    val refreshTokenExpirationTime: Long
)
