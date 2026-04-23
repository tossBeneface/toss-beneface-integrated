package com.app.global.jwt.constant

enum class TokenType {
    ACCESS, REFRESH;

    companion object {
        fun isAccessToken(tokenType: String): Boolean {
            return ACCESS.name == tokenType
        }
    }
}
