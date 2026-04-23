package com.app.payment.application.port

import java.util.*

interface BillingKeyStore {
    fun save(customerKey: String, billingKey: String)
    fun find(customerKey: String): Optional<String>
}
