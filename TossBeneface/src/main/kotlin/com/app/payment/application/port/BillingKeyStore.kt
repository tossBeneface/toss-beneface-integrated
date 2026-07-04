package com.app.payment.application.port

import java.util.*

/**
 * Stores short-lived billing keys returned by Toss Billing Authorization.
 *
 * Production uses a shared Redis adapter because billing-key confirmation can
 * run on another backend instance after callback.
 */
interface BillingKeyStore {
    fun save(customerKey: String, billingKey: String)
    fun find(customerKey: String): Optional<String>
}
