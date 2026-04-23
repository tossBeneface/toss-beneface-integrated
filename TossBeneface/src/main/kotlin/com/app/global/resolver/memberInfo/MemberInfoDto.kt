package com.app.global.resolver.memberInfo

import com.app.domain.member.constant.Role

data class MemberInfoDto(
    val memberId: Long,
    val memberName: String = "",
    val email: String = "",
    val role: Role
)
