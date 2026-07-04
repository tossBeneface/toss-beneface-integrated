package com.app.api.login.mapper

import com.app.api.login.dto.JoinDto
import com.app.api.login.dto.LoginDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AuthRequestMapperTest {

    private val mapper = AuthRequestMapper()

    @Test
    fun `maps login request to command`() {
        val command = mapper.toCommand(
            LoginDto.Request(
                email = "member@example.com",
                password = "password"
            )
        )

        assertEquals("member@example.com", command.email)
        assertEquals("password", command.password)
    }

    @Test
    fun `maps join request to command`() {
        val command = mapper.toCommand(
            JoinDto.Request(
                email = "member@example.com",
                password = "password",
                memberName = "member",
                phoneNumber = "010-1234-5678",
                gender = "male",
                profileImg = "profile.png",
                role = "user"
            )
        )

        assertEquals("member@example.com", command.email)
        assertEquals("password", command.password)
        assertEquals("member", command.memberName)
        assertEquals("010-1234-5678", command.phoneNumber)
        assertEquals("male", command.gender)
        assertEquals("profile.png", command.profileImg)
        assertEquals("user", command.role)
    }
}
