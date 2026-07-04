package com.app.auth.infra.web

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.springframework.mock.web.MockHttpServletResponse

class AccessTokenCookieManagerTest {

    private val manager = AccessTokenCookieManager(60_000)

    @Test
    fun `adds access token cookie`() {
        val response = MockHttpServletResponse()

        manager.addAccessTokenCookie(response, "access-token")

        val cookieHeader = response.getHeader("Set-Cookie")
        assertTrue(cookieHeader!!.contains("accessToken=access-token"))
        assertTrue(cookieHeader.contains("HttpOnly"))
        assertTrue(cookieHeader.contains("Secure"))
        assertTrue(cookieHeader.contains("SameSite=None"))
        assertTrue(cookieHeader.contains("Max-Age=60"))
    }

    @Test
    fun `removes access token cookie`() {
        val response = MockHttpServletResponse()

        manager.removeAccessTokenCookie(response)

        val cookieHeader = response.getHeader("Set-Cookie")
        assertTrue(cookieHeader!!.contains("accessToken="))
        assertTrue(cookieHeader.contains("Max-Age=0"))
    }
}
