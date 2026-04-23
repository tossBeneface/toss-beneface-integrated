package com.app.domain.member.constant

enum class MemberStatus {
    ACTIVATE, DEACTIVATE;

    companion object {
        fun from(memberStatus: String): MemberStatus {
            return valueOf(memberStatus)
        }
    }
}
