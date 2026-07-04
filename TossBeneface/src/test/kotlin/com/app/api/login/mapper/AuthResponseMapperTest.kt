package com.app.api.login.mapper

import com.app.auth.application.dto.AuthSessionResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Date

class AuthResponseMapperTest {

    private val mapper = AuthResponseMapper()

    @Test
    fun `maps session result to login response`() {
        val accessTokenExpireTime = Date(System.currentTimeMillis() + 60_000)
        val refreshTokenExpireTime = Date(System.currentTimeMillis() + 120_000)

        val response = mapper.toLoginResponse(
            AuthSessionResult(
                memberId = "42",
                email = "member@example.com",
                memberName = "member",
                phoneNumber = "010-1234-5678",
                gender = "MALE",
                profileImg = "profile.png",
                role = "USER",
                grantType = "Bearer",
                accessToken = "access-token",
                accessTokenExpireTime = accessTokenExpireTime,
                refreshToken = "refresh-token",
                refreshTokenExpireTime = refreshTokenExpireTime
            )
        )

        assertEquals("42", response.memberId)
        assertEquals("member@example.com", response.email)
        assertEquals("member", response.memberName)
        assertEquals("010-1234-5678", response.phoneNumber)
        assertEquals("MALE", response.gender)
        assertEquals("profile.png", response.profileImg)
        assertEquals("USER", response.role)
        assertEquals("Bearer", response.grantType)
        assertEquals("access-token", response.accessToken)
        assertEquals(accessTokenExpireTime, response.accessTokenExpireTime)
        assertEquals("refresh-token", response.refreshToken)
        assertEquals(refreshTokenExpireTime, response.refreshTokenExpireTime)
    }

    @Test
    fun `maps session result to join response`() {
        val accessTokenExpireTime = Date(System.currentTimeMillis() + 60_000)
        val refreshTokenExpireTime = Date(System.currentTimeMillis() + 120_000)

        val response = mapper.toJoinResponse(
            AuthSessionResult(
                memberId = "42",
                email = "member@example.com",
                memberName = "member",
                phoneNumber = "010-1234-5678",
                gender = "male",
                role = "user",
                grantType = "Bearer",
                accessToken = "access-token",
                accessTokenExpireTime = accessTokenExpireTime,
                refreshToken = "refresh-token",
                refreshTokenExpireTime = refreshTokenExpireTime
            )
        )

        assertEquals("42", response.memberId)
        assertEquals("member@example.com", response.email)
        assertEquals("member", response.memberName)
        assertEquals("010-1234-5678", response.phoneNumber)
        assertEquals("male", response.gender)
        assertEquals("user", response.role)
        assertEquals("Bearer", response.grantType)
        assertEquals("access-token", response.accessToken)
        assertEquals(accessTokenExpireTime, response.accessTokenExpireTime)
        assertEquals("refresh-token", response.refreshToken)
        assertEquals(refreshTokenExpireTime, response.refreshTokenExpireTime)
    }
}
