package com.app.api.member.mapper

import com.app.member.application.dto.MemberInfoResult
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MemberInfoResponseMapperTest {

    private val mapper = MemberInfoResponseMapper()

    @Test
    fun `maps member info result to response dto`() {
        val response = mapper.toResponse(
            MemberInfoResult(
                memberId = "42",
                email = "member@example.com",
                memberName = "Existing User",
                phoneNumber = "010-9999-0000",
                gender = "MALE",
                budget = 50_000,
                profileImg = "profile.png",
                role = "USER"
            )
        )

        assertEquals("42", response.memberId)
        assertEquals("member@example.com", response.email)
        assertEquals("Existing User", response.memberName)
        assertEquals("010-9999-0000", response.phoneNumber)
        assertEquals("MALE", response.gender)
        assertEquals(50_000, response.budget)
        assertEquals("profile.png", response.profileImg)
        assertEquals("USER", response.role)
    }
}
