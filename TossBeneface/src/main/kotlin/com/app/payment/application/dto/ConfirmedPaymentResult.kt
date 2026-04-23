package com.app.payment.application.dto

import com.app.domain.payment.entity.PaymentStatus

/**
 * 결제 승인 성공 시의 결과 데이터
 */
data class ConfirmedPaymentResult(
    val memberId: Long,
    val paymentKey: String,
    val orderId: String,
    val orderName: String,
    val method: String,
    val totalAmount: Int,
    val status: PaymentStatus,
    val requestedAt: String?,
    val approvedAt: String?,
    val receiptUrl: String?
)
