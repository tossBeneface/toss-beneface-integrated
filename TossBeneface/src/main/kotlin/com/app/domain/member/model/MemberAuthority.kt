package com.app.domain.member.model

import com.app.domain.member.constant.Role

data class MemberAuthority(
    val role: Role = Role.USER
)
