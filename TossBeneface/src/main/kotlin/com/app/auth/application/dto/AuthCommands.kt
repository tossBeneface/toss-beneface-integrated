package com.app.auth.application.dto

data class LoginCommand(
    val email: String,
    val password: String
)

data class JoinCommand(
    val email: String,
    val password: String,
    val memberName: String,
    val phoneNumber: String,
    val gender: String,
    val profileImg: String?,
    val role: String
)
