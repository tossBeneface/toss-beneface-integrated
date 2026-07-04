package com.app.payment.infra.store

import com.app.payment.application.port.BillingKeyStore
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.Optional

@Component
@Profile("prod")
class RedisBillingKeyStore(
    private val redisTemplate: StringRedisTemplate,
    @Value("\${payment.billing-key-ttl:PT10M}") private val billingKeyTtl: Duration
) : BillingKeyStore {

    override fun save(customerKey: String, billingKey: String) {
        redisTemplate.opsForValue().set(key(customerKey), billingKey, billingKeyTtl)
    }

    override fun find(customerKey: String): Optional<String> {
        return Optional.ofNullable(redisTemplate.opsForValue().get(key(customerKey)))
    }

    private fun key(customerKey: String): String {
        return "payment:billing-key:$customerKey"
    }
}
