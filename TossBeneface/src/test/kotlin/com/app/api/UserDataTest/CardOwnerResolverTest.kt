package com.app.api.UserDataTest

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class CardOwnerResolverTest {

    private val repository = mockk<UserDataTestRepository>()
    private val resolver = CardOwnerResolver(repository)

    @Test
    fun `returns owner member id`() {
        every { repository.findOwnerMemberIdById(10L) } returns 7L
        assertEquals(7L, resolver.ownerOf(10L))
    }

    @Test
    fun `returns null when card not found`() {
        every { repository.findOwnerMemberIdById(10L) } returns null
        assertNull(resolver.ownerOf(10L))
    }
}
