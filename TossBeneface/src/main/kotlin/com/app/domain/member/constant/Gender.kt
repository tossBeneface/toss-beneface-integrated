package com.app.domain.member.constant

enum class Gender {
    MALE, FEMALE, UNKNOWN;

    companion object {
        fun from(gender: String): Gender {
            return valueOf(gender)
        }
    }
}
