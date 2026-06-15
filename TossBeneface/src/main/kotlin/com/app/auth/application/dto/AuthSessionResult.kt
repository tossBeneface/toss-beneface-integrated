package com.app.auth.application.dto

import java.util.Date

data class AuthSessionResult(
    val memberId: String?,
    val email: String,
    val memberName: String,
    val phoneNumber: String,
    val gender: String,
    val budget: String? = null,
    val profileImg: String? = null,
    val role: String,
    val grantType: String,
    val accessToken: String?,
    val accessTokenExpireTime: Date?,
    val refreshToken: String?,
    val refreshTokenExpireTime: Date?
)
