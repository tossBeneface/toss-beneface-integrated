package com.app.auth.infra.security

import com.app.domain.member.constant.Role

data class AuthenticatedMemberContext(
    val memberId: Long,
    val role: Role
)
