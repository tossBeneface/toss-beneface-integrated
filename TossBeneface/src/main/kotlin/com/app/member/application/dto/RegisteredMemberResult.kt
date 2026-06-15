package com.app.member.application.dto

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member

data class RegisteredMemberResult(
    val memberId: Long,
    val email: String,
    val memberName: String,
    val phoneNumber: String,
    val gender: Gender,
    val budget: Int,
    val profileImg: String?,
    val role: Role,
    val memberStatus: MemberStatus
) {
    companion object {
        fun from(member: Member): RegisteredMemberResult {
            return RegisteredMemberResult(
                memberId = requireNotNull(member.memberId) { "Member id is missing" },
                email = member.email,
                memberName = member.memberName,
                phoneNumber = member.phoneNumber,
                gender = member.gender,
                budget = member.budget ?: 0,
                profileImg = member.profileImg,
                role = member.role,
                memberStatus = member.memberStatus
            )
        }
    }
}
