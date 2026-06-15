package com.app.member.application.dto

import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.model.MemberOnboarding

data class RegisterMemberCommand(
    val identity: RegisterMemberIdentity,
    val profile: RegisterMemberProfile,
    val authority: RegisterMemberAuthority = RegisterMemberAuthority(),
    val onboarding: RegisterMemberOnboarding = RegisterMemberOnboarding()
)

data class RegisterMemberIdentity(
    val email: String,
    val encodedPassword: String
)

data class RegisterMemberProfile(
    val memberName: String,
    val phoneNumber: String,
    val gender: String,
    val profileImg: String?
)

data class RegisterMemberAuthority(
    val role: String = Role.USER.name
)

data class RegisterMemberOnboarding(
    val initialBudget: Int = MemberOnboarding.DEFAULT_INITIAL_BUDGET,
    val status: String = MemberStatus.ACTIVATE.name
)

data class ResolveSocialMemberCommand(
    val socialType: String,
    val socialId: String,
    val email: String,
    val memberName: String
)
