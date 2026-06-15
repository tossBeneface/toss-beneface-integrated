package com.app.payment.infra.toss

import com.app.domain.payment.entity.PaymentStatus
import com.app.payment.application.dto.ConfirmedPaymentResult
import com.app.payment.application.dto.PaymentResult
import com.app.payment.application.dto.TossApiResponse
import org.springframework.stereotype.Component

@Component
class TossPaymentResponseMapper {

    fun toPaymentResult(response: TossApiResponse, memberId: Long?): PaymentResult {
        return if (response.isSuccess()) {
            PaymentResult.Success(toConfirmedPaymentResult(response, memberId))
        } else {
            PaymentResult.Failure(
                errorCode = response.body["code"]?.toString() ?: "UNKNOWN_ERROR",
                message = response.body["message"]?.toString() ?: "Toss API Error"
            )
        }
    }

    private fun toConfirmedPaymentResult(response: TossApiResponse, memberId: Long?): ConfirmedPaymentResult {
        val body = response.body
        return ConfirmedPaymentResult(
            memberId = memberId ?: (body["memberId"]?.toString()?.toLong() ?: 0L),
            paymentKey = body["paymentKey"].toString(),
            orderId = body["orderId"].toString(),
            orderName = body["orderName"].toString(),
            method = body["method"].toString(),
            totalAmount = body["totalAmount"].toString().toInt(),
            status = PaymentStatus.from(body["status"]?.toString()),
            requestedAt = body["requestedAt"]?.toString(),
            approvedAt = body["approvedAt"]?.toString(),
            receiptUrl = extractReceiptUrl(body)
        )
    }

    @Suppress("UNCHECKED_CAST")
    private fun extractReceiptUrl(body: Map<String, Any>): String? {
        val receipt = body["receipt"] as? Map<String, Any> ?: return null
        return receipt["url"]?.toString()
    }
}
