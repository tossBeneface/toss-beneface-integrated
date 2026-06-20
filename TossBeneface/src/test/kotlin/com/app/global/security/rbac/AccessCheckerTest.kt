package com.app.global.security.rbac

import com.app.auth.infra.security.AuthenticatedMemberContext
import com.app.auth.infra.security.AuthenticatedMemberContextResolver
import com.app.domain.member.constant.Role
import io.mockk.every
import io.mockk.mockk
import org.casbin.jcasbin.main.Enforcer
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class AccessCheckerTest {

    private val enforcer = mockk<Enforcer>()
    private val contextResolver = mockk<AuthenticatedMemberContextResolver>()
    private val cardResolver = mockk<ResourceOwnerResolver>()
    private val accessChecker = AccessChecker(
        enforcer, contextResolver, mapOf("card" to cardResolver)
    )

    private fun loginAs(memberId: Long, role: Role) {
        every { contextResolver.currentContextOrNull() } returns AuthenticatedMemberContext(memberId, role)
    }

    @Test
    fun `role-only allow when enforcer grants any`() {
        loginAs(1L, Role.ADMIN)
        every { enforcer.enforce("ADMIN", "member", "manage", "any") } returns true
        assertTrue(accessChecker.can("member", "manage"))
    }

    @Test
    fun `deny when not authenticated`() {
        every { contextResolver.currentContextOrNull() } returns null
        assertFalse(accessChecker.can("member", "manage"))
    }

    @Test
    fun `ownership allow when owner matches`() {
        loginAs(5L, Role.USER)
        every { enforcer.enforce("USER", "card", "delete", "any") } returns false
        every { enforcer.enforce("USER", "card", "delete", "own") } returns true
        every { cardResolver.ownerOf(10L) } returns 5L
        assertTrue(accessChecker.can("card", "delete", 10L))
    }

    @Test
    fun `ownership deny when owner differs`() {
        loginAs(5L, Role.USER)
        every { enforcer.enforce("USER", "card", "delete", "any") } returns false
        every { enforcer.enforce("USER", "card", "delete", "own") } returns true
        every { cardResolver.ownerOf(10L) } returns 99L
        assertFalse(accessChecker.can("card", "delete", 10L))
    }

    @Test
    fun `ownership deny when no permission at all`() {
        loginAs(5L, Role.USER)
        every { enforcer.enforce("USER", "card", "delete", "any") } returns false
        every { enforcer.enforce("USER", "card", "delete", "own") } returns false
        assertFalse(accessChecker.can("card", "delete", 10L))
    }

    @Test
    fun `any scope overrides ownership without resolver`() {
        loginAs(1L, Role.ADMIN)
        every { enforcer.enforce("ADMIN", "card", "delete", "any") } returns true
        assertTrue(accessChecker.can("card", "delete", 10L))
    }

    @Test
    fun `ownership deny when resource not found`() {
        loginAs(5L, Role.USER)
        every { enforcer.enforce("USER", "card", "delete", "any") } returns false
        every { enforcer.enforce("USER", "card", "delete", "own") } returns true
        every { cardResolver.ownerOf(10L) } returns null
        assertFalse(accessChecker.can("card", "delete", 10L))
    }

    @Test
    fun `throws when no resolver registered for owned resource`() {
        loginAs(5L, Role.USER)
        every { enforcer.enforce("USER", "order", "cancel", "any") } returns false
        every { enforcer.enforce("USER", "order", "cancel", "own") } returns true
        assertThrows(IllegalStateException::class.java) {
            accessChecker.can("order", "cancel", 10L)
        }
    }
}
