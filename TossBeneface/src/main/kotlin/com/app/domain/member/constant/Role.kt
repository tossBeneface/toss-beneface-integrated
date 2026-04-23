package com.app.domain.member.constant

import java.util.*

enum class Role {
    USER, OWNER, ADMIN;

    companion object {
        fun from(role: String?): Role {
            if (role == null) {
                throw IllegalArgumentException("role must not be null")
            }
            return valueOf(role.uppercase(Locale.ROOT))
        }
    }
}
