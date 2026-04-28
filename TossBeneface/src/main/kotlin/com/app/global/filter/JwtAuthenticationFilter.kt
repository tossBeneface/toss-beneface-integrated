package com.app.global.filter

import com.app.auth.infra.security.JwtTokenProvider
import com.app.auth.infra.web.BearerTokenResolver
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val bearerTokenResolver: BearerTokenResolver,
    private val jwtTokenProvider: JwtTokenProvider
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val token = bearerTokenResolver.resolve(request)
            val claims = jwtTokenProvider.parseAccessToken(token)
            val memberId = jwtTokenProvider.extractMemberId(claims)
            val role = jwtTokenProvider.extractRole(claims)

            val authorities = listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
            val authentication = UsernamePasswordAuthenticationToken.authenticated(memberId, token, authorities)

            SecurityContextHolder.getContext().authentication = authentication
        } catch (e: Exception) {
            // 토큰이 없거나 유효하지 않은 경우 SecurityContext를 채우지 않고 다음 필터로 진행
            // (SecurityConfig에서 설정한 permitAll/authenticated 설정에 따라 처리됨)
            SecurityContextHolder.clearContext()
        }

        filterChain.doFilter(request, response)
    }

    override fun shouldNotFilter(request: HttpServletRequest): Boolean {
        val path = request.requestURI
        return path.startsWith("/swagger-ui") || 
               path.startsWith("/v3/api-docs") || 
               path.startsWith("/h2-console") ||
               path.startsWith("/ws")
    }
}
