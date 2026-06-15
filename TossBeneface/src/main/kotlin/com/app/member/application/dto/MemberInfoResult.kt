package com.app.member.application.dto

data class MemberInfoResult(
    val memberId: String,
    val email: String,
    val memberName: String,
    val phoneNumber: String,
    val gender: String,
    val budget: Int,
    val profileImg: String?,
    val role: String
)
