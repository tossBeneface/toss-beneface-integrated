package com.app.global.resolver.memberInfo

import com.app.auth.infra.security.JwtTokenProvider
import com.app.auth.infra.web.BearerTokenResolver
import com.app.domain.member.constant.Role
import io.jsonwebtoken.Claims
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.core.MethodParameter
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.context.request.ServletWebRequest

class MemberInfoArgumentResolverTest {

    private val bearerTokenResolver = mockk<BearerTokenResolver>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val resolver = MemberInfoArgumentResolver(bearerTokenResolver, jwtTokenProvider)
    private val methodParameter = MethodParameter(
        TestController::class.java.getDeclaredMethod("endpoint", MemberInfoDto::class.java),
        0
    )

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `resolves member info from security context first`() {
        val authentication = UsernamePasswordAuthenticationToken.authenticated(
            7L,
            "token",
            listOf(SimpleGrantedAuthority("ROLE_ADMIN"))
        )
        SecurityContextHolder.getContext().authentication = authentication

        val result = resolver.resolveArgument(
            methodParameter,
            null,
            ServletWebRequest(MockHttpServletRequest()),
            null
        ) as MemberInfoDto

        assertEquals(7L, result.memberId)
        assertEquals(Role.ADMIN, result.role)
    }

    @Test
    fun `falls back to access token parsing when security context is empty`() {
        val request = MockHttpServletRequest()
        val claims = mockk<Claims>()

        every { bearerTokenResolver.resolve(any<HttpServletRequest>()) } returns "valid-token"
        every { jwtTokenProvider.parseAccessToken("valid-token") } returns claims
        every { jwtTokenProvider.extractMemberId(claims) } returns 11L
        every { jwtTokenProvider.extractRole(claims) } returns Role.USER

        val result = resolver.resolveArgument(
            methodParameter,
            null,
            ServletWebRequest(request),
            null
        ) as MemberInfoDto

        assertEquals(11L, result.memberId)
        assertEquals(Role.USER, result.role)
    }

    private class TestController {
        @Suppress("unused")
        fun endpoint(@MemberInfo memberInfoDto: MemberInfoDto) {
        }
    }
}
