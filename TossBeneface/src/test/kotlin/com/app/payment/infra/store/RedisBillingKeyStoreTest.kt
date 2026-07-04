package com.app.payment.infra.store

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.data.redis.core.ValueOperations
import java.time.Duration

class RedisBillingKeyStoreTest {

    private val redisTemplate = mock(StringRedisTemplate::class.java)

    @Suppress("UNCHECKED_CAST")
    private val valueOperations = mock(ValueOperations::class.java) as ValueOperations<String, String>
    private val store = RedisBillingKeyStore(redisTemplate, Duration.ofMinutes(10))

    @Test
    fun savesBillingKeyWithTtl() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations)

        store.save("customer-1", "billing-key-1")

        verify(valueOperations).set(
            "payment:billing-key:customer-1",
            "billing-key-1",
            Duration.ofMinutes(10)
        )
    }

    @Test
    fun findsBillingKey() {
        given(redisTemplate.opsForValue()).willReturn(valueOperations)
        given(valueOperations.get("payment:billing-key:customer-1")).willReturn("billing-key-1")

        val billingKey = store.find("customer-1")

        assertTrue(billingKey.isPresent)
        assertEquals("billing-key-1", billingKey.get())
    }
}
