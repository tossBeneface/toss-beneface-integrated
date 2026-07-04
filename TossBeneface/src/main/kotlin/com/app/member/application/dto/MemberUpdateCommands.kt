package com.app.member.application.dto

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member

data class UpdateMemberProfileCommand(
    val memberId: Long,
    val memberName: String,
    val phoneNumber: String,
    val gender: String,
    val profileImg: String?
)

data class ChangeMemberAuthorityCommand(
    val memberId: Long,
    val role: String
)

data class UpdateMemberInitialStateCommand(
    val memberId: Long,
    val initialBudget: Int,
    val memberStatus: String
)

data class MemberProfileResult(
    val memberId: Long,
    val memberName: String,
    val phoneNumber: String,
    val gender: Gender,
    val profileImg: String?
) {
    companion object {
        fun from(member: Member): MemberProfileResult {
            return MemberProfileResult(
                memberId = requireNotNull(member.memberId) { "Member id is missing" },
                memberName = member.memberName,
                phoneNumber = member.phoneNumber,
                gender = member.gender,
                profileImg = member.profileImg
            )
        }
    }
}

data class MemberAuthorityResult(
    val memberId: Long,
    val role: Role
) {
    companion object {
        fun from(member: Member): MemberAuthorityResult {
            return MemberAuthorityResult(
                memberId = requireNotNull(member.memberId) { "Member id is missing" },
                role = member.role
            )
        }
    }
}

data class MemberInitialStateResult(
    val memberId: Long,
    val budget: Int,
    val memberStatus: MemberStatus
) {
    companion object {
        fun from(member: Member): MemberInitialStateResult {
            return MemberInitialStateResult(
                memberId = requireNotNull(member.memberId) { "Member id is missing" },
                budget = member.budget ?: 0,
                memberStatus = member.memberStatus
            )
        }
    }
}
