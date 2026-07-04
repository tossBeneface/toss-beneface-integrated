package com.app.member.application.dto

import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.model.MemberInitialState

data class RegisterMemberCommand(
    val identity: RegisterMemberIdentity,
    val profile: RegisterMemberProfile,
    val authority: RegisterMemberAuthority = RegisterMemberAuthority(),
    val initialState: RegisterMemberInitialState = RegisterMemberInitialState()
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

data class RegisterMemberInitialState(
    val initialBudget: Int = MemberInitialState.DEFAULT_INITIAL_BUDGET,
    val memberStatus: String = MemberStatus.ACTIVATE.name
)

data class ResolveSocialMemberCommand(
    val socialType: String,
    val socialId: String,
    val email: String,
    val memberName: String
)
