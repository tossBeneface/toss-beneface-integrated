package com.app.payment.infra.store

import com.app.payment.application.port.BillingKeyStore
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryBillingKeyStore : BillingKeyStore {

    private val billingKeys = ConcurrentHashMap<String, String>()

    override fun save(customerKey: String, billingKey: String) {
        billingKeys[customerKey] = billingKey
    }

    override fun find(customerKey: String): Optional<String> {
        return Optional.ofNullable(billingKeys[customerKey])
    }
}
