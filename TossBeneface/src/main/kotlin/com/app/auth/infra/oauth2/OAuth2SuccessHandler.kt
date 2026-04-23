package com.app.auth.infra.oauth2

import com.app.domain.member.constant.Role
import com.app.global.jwt.service.TokenManager
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseCookie
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class OAuth2SuccessHandler(
    private val tokenManager: TokenManager,
    @Value("\${app.oauth2.redirect-uri:http://localhost:3000}") private val redirectUri: String
) : SimpleUrlAuthenticationSuccessHandler() {

    override fun onAuthenticationSuccess(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authentication: Authentication
    ) {
        val principal = authentication.principal as OAuth2User
        val memberId = principal.attributes["memberId"].toString().toLong()
        val role = Role.valueOf(principal.attributes["role"].toString())

        val tokenDto = tokenManager.createJwtTokenDto(memberId, role, response)
        val accessToken = tokenDto.accessToken ?: throw IllegalStateException("Access token is missing")

        val accessTokenCookie = ResponseCookie.from(ACCESS_TOKEN_COOKIE_NAME, accessToken)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(Duration.ofMillis(tokenManager.accessTokenExpirationTime.toLong()))
            .build()

        response.addHeader("Set-Cookie", accessTokenCookie.toString())
        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, redirectUri)
    }

    companion object {
        private const val ACCESS_TOKEN_COOKIE_NAME = "accessToken"
    }
}
