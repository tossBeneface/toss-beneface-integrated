package com.app.global.security.rbac

import org.casbin.jcasbin.main.Enforcer
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CasbinPolicyTest {

    private val enforcer = Enforcer(
        "src/main/resources/rbac/model.conf",
        "src/main/resources/rbac/policy.csv"
    )

    @Test
    fun `admin can manage member`() {
        assertTrue(enforcer.enforce("ADMIN", "member", "manage", "any"))
    }

    @Test
    fun `user cannot manage member`() {
        assertFalse(enforcer.enforce("USER", "member", "manage", "any"))
    }

    @Test
    fun `admin can manage benefit`() {
        assertTrue(enforcer.enforce("ADMIN", "benefit", "manage", "any"))
    }

    @Test
    fun `user can delete own card`() {
        assertTrue(enforcer.enforce("USER", "card", "delete", "own"))
    }

    @Test
    fun `user cannot delete any card`() {
        assertFalse(enforcer.enforce("USER", "card", "delete", "any"))
    }

    @Test
    fun `admin can delete any card via inheritance and explicit policy`() {
        assertTrue(enforcer.enforce("ADMIN", "card", "delete", "any"))
        assertTrue(enforcer.enforce("ADMIN", "card", "delete", "own"))
    }
}
