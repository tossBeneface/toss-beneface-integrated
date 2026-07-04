package com.app.auth.infra.web

import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class AccessTokenCookieManager(
    @Value("\${token.access-token-expiration-time}") private val accessTokenExpirationTime: Long
) {
    fun addAccessTokenCookie(response: HttpServletResponse, accessToken: String) {
        val accessTokenCookie = ResponseCookie.from(AuthCookieNames.ACCESS_TOKEN, accessToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(Duration.ofMillis(accessTokenExpirationTime))
            .build()

        response.addHeader("Set-Cookie", accessTokenCookie.toString())
    }

    fun removeAccessTokenCookie(response: HttpServletResponse) {
        val accessTokenCookie = ResponseCookie.from(AuthCookieNames.ACCESS_TOKEN, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(0)
            .build()

        response.addHeader("Set-Cookie", accessTokenCookie.toString())
    }
}
