package com.app.auth.infra.security

import com.app.auth.infra.web.BearerTokenResolver
import com.app.domain.member.constant.Role
import jakarta.servlet.http.HttpServletRequest
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component

@Component
class AuthenticatedMemberContextResolver(
    private val bearerTokenResolver: BearerTokenResolver,
    private val jwtTokenProvider: JwtTokenProvider
) {
    fun resolve(request: HttpServletRequest): AuthenticatedMemberContext {
        resolveFromSecurityContext()?.let { return it }

        val token = bearerTokenResolver.resolve(request)
        val tokenClaims = jwtTokenProvider.parseAccessToken(token)
        return AuthenticatedMemberContext(
            memberId = jwtTokenProvider.extractMemberId(tokenClaims),
            role = jwtTokenProvider.extractRole(tokenClaims)
        )
    }

    private fun resolveFromSecurityContext(): AuthenticatedMemberContext? {
        val authentication = SecurityContextHolder.getContext().authentication ?: return null
        if (!authentication.isAuthenticated) {
            return null
        }

        val memberId = when (val principal = authentication.principal) {
            is Long -> principal
            is Int -> principal.toLong()
            is String -> principal.toLongOrNull()
            else -> null
        } ?: return null

        val role = authentication.authorities
            .asSequence()
            .map { it.authority.removePrefix("ROLE_") }
            .mapNotNull {
                runCatching { Role.from(it) }.getOrNull()
            }
            .firstOrNull()
            ?: return null

        return AuthenticatedMemberContext(memberId, role)
    }
}
