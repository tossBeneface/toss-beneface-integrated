package com.app.domain.member.model

data class MemberIdentity(
    val email: String,
    val password: String? = null,
    val socialType: String? = null,
    val socialId: String? = null
) {
    companion object {
        fun local(email: String, encodedPassword: String): MemberIdentity {
            return MemberIdentity(
                email = email,
                password = encodedPassword
            )
        }

        fun social(email: String, socialType: String, socialId: String): MemberIdentity {
            return MemberIdentity(
                email = email,
                socialType = socialType,
                socialId = socialId
            )
        }
    }
}
