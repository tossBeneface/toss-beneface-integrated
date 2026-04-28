package com.app.global.interceptor

import com.app.auth.infra.security.JwtTokenProvider
import com.app.auth.infra.web.BearerTokenResolver
import com.app.domain.member.constant.Role
import com.app.global.error.ErrorCode
import com.app.global.error.exception.AuthenticationException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AdminAuthorizationInterceptor(
    private val bearerTokenResolver: BearerTokenResolver,
    private val jwtTokenProvider: JwtTokenProvider
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication?.isAuthenticated == true) {
            val isAdmin = authentication.authorities.any { it.authority == "ROLE_${Role.ADMIN.name}" }
            if (!isAdmin) {
                throw AuthenticationException(ErrorCode.FORBIDDEN_ADMIN)
            }
            return true
        }

        val accessToken = bearerTokenResolver.resolve(request)
        val tokenClaims = jwtTokenProvider.parseAccessToken(accessToken)
        val role = jwtTokenProvider.extractRole(tokenClaims)

        if (Role.ADMIN != role) {
            throw AuthenticationException(ErrorCode.FORBIDDEN_ADMIN)
        }

        return true
    }
}
