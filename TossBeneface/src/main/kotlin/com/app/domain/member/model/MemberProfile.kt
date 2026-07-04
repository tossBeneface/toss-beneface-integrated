package com.app.domain.member.model

import com.app.domain.member.constant.Gender

data class MemberProfile(
    val memberName: String,
    val phoneNumber: String,
    val gender: Gender,
    val profileImg: String? = null
) {
    companion object {
        fun social(memberName: String): MemberProfile {
            return MemberProfile(
                memberName = memberName,
                phoneNumber = DEFAULT_SOCIAL_PHONE_NUMBER,
                gender = Gender.UNKNOWN,
                profileImg = null
            )
        }

        private const val DEFAULT_SOCIAL_PHONE_NUMBER = "000-0000-0000"
    }
}
