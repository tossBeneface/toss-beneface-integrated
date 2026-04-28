package com.app.global.filter

import com.app.auth.infra.security.JwtTokenProvider
import com.app.auth.infra.web.BearerTokenResolver
import com.app.domain.member.constant.Role
import io.jsonwebtoken.Claims
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.FilterChain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.mock.web.MockHttpServletResponse
import org.springframework.security.core.context.SecurityContextHolder

class JwtAuthenticationFilterTest {

    private val bearerTokenResolver = mockk<BearerTokenResolver>()
    private val jwtTokenProvider = mockk<JwtTokenProvider>()
    private val filter = JwtAuthenticationFilter(bearerTokenResolver, jwtTokenProvider)

    @AfterEach
    fun tearDown() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `fills security context when access token is valid`() {
        val request = MockHttpServletRequest("GET", "/api/orders")
        val response = MockHttpServletResponse()
        val chain = RecordingFilterChain()
        val claims = mockk<Claims>()

        every { bearerTokenResolver.resolve(request) } returns "valid-token"
        every { jwtTokenProvider.parseAccessToken("valid-token") } returns claims
        every { jwtTokenProvider.extractMemberId(claims) } returns 1L
        every { jwtTokenProvider.extractRole(claims) } returns Role.USER

        filter.doFilter(request, response, chain)

        val authentication = SecurityContextHolder.getContext().authentication
        assertTrue(chain.called)
        assertEquals(1L, authentication.principal)
        assertEquals("valid-token", authentication.credentials)
        assertTrue(authentication.authorities.any { it.authority == "ROLE_USER" })
    }

    @Test
    fun `continues filter chain without authentication when token is missing or invalid`() {
        val request = MockHttpServletRequest("GET", "/api/orders")
        val response = MockHttpServletResponse()
        val chain = RecordingFilterChain()

        every { bearerTokenResolver.resolve(request) } throws IllegalArgumentException("missing token")

        filter.doFilter(request, response, chain)

        assertTrue(chain.called)
        assertNull(SecurityContextHolder.getContext().authentication)
    }

    private class RecordingFilterChain : FilterChain {
        var called: Boolean = false

        override fun doFilter(request: jakarta.servlet.ServletRequest?, response: jakarta.servlet.ServletResponse?) {
            called = true
        }
    }
}
