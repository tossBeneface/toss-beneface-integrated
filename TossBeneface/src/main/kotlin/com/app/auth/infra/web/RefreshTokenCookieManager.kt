package com.app.auth.infra.web

import com.app.global.error.ErrorCode
import com.app.global.error.exception.AuthenticationException
import com.app.global.util.CookieEncryptionUtils
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseCookie
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.*

@Component
class RefreshTokenCookieManager(
    private val cookieEncryptionUtils: CookieEncryptionUtils
) {
    private val log = LoggerFactory.getLogger(RefreshTokenCookieManager::class.java)

    fun extractRefreshToken(request: HttpServletRequest): String {
        val cookies = request.cookies ?: throw AuthenticationException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)

        val encryptedRefreshToken = Arrays.stream(cookies)
            .filter { cookie -> REFRESH_TOKEN_COOKIE_NAME == cookie.name }
            .map { it.value }
            .findFirst()
            .orElseThrow { AuthenticationException(ErrorCode.REFRESH_TOKEN_NOT_FOUND) }

        return try {
            cookieEncryptionUtils.decrypt(encryptedRefreshToken)
        } catch (e: Exception) {
            if (looksLikeJwt(encryptedRefreshToken)) {
                log.warn("Using legacy raw refresh token cookie")
                encryptedRefreshToken
            } else {
                log.error("Failed to decrypt refresh token", e)
                throw AuthenticationException(ErrorCode.REFRESH_TOKEN_NOT_FOUND)
            }
        }
    }

    fun addRefreshTokenCookie(response: HttpServletResponse, refreshToken: String, expirationTime: Long) {
        val encryptedValue = cookieEncryptionUtils.encrypt(refreshToken)

        val refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, encryptedValue)
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(Duration.ofMillis(expirationTime))
            .build()

        response.addHeader("Set-Cookie", refreshTokenCookie.toString())
    }

    fun removeRefreshTokenCookie(response: HttpServletResponse) {
        val refreshTokenCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
            .httpOnly(true)
            .secure(true)
            .sameSite("None")
            .path("/")
            .maxAge(0)
            .build()

        response.addHeader("Set-Cookie", refreshTokenCookie.toString())
    }

    private fun looksLikeJwt(tokenValue: String?): Boolean {
        return tokenValue != null && tokenValue.chars().filter { ch -> ch == '.'.code }.count() == 2L
    }

    companion object {
        private const val REFRESH_TOKEN_COOKIE_NAME = "refreshToken"
    }
}
