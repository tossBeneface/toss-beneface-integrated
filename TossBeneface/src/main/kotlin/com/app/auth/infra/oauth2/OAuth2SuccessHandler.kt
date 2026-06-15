package com.app.auth.infra.oauth2

import com.app.auth.application.service.TokenManager
import com.app.auth.infra.web.AccessTokenCookieManager
import com.app.auth.infra.web.RefreshTokenCookieManager
import com.app.domain.member.constant.Role
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler
import org.springframework.stereotype.Component

@Component
class OAuth2SuccessHandler(
    private val tokenManager: TokenManager,
    private val accessTokenCookieManager: AccessTokenCookieManager,
    private val refreshTokenCookieManager: RefreshTokenCookieManager,
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

        val tokenDto = tokenManager.createJwtTokenDto(memberId, role)
        accessTokenCookieManager.addAccessTokenCookie(
            response,
            requireNotNull(tokenDto.accessToken) { "Access token is missing" }
        )
        refreshTokenCookieManager.addRefreshTokenCookie(
            response,
            requireNotNull(tokenDto.refreshToken) { "Refresh token is missing" },
            requireNotNull(tokenDto.refreshTokenExpireTime) { "Refresh token expiration time is missing" }
        )

        clearAuthenticationAttributes(request)
        redirectStrategy.sendRedirect(request, response, redirectUri)
    }
}
