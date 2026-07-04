package com.app.payment.application.service

import com.app.domain.member.entity.Member
import com.app.domain.payment.entity.Payment
import com.app.payment.application.dto.ConfirmedPaymentResult
import org.springframework.stereotype.Component

@Component
class PaymentFactory {

    fun create(confirmedPayment: ConfirmedPaymentResult, member: Member): Payment {
        return Payment(
            paymentKey = confirmedPayment.paymentKey,
            orderId = confirmedPayment.orderId,
            orderName = confirmedPayment.orderName,
            method = confirmedPayment.method,
            totalAmount = confirmedPayment.totalAmount,
            status = confirmedPayment.status,
            requestedAt = confirmedPayment.requestedAt,
            approvedAt = confirmedPayment.approvedAt,
            receiptUrl = confirmedPayment.receiptUrl,
            member = member
        )
    }
}
