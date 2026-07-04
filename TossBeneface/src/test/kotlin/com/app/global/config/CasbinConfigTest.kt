package com.app.global.config

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class CasbinConfigTest {

    private val enforcer = CasbinConfig().enforcer()

    @Test
    fun `loads model and policy from classpath`() {
        assertTrue(enforcer.enforce("ADMIN", "member", "manage", "any"))
        assertFalse(enforcer.enforce("USER", "member", "manage", "any"))
    }
}
