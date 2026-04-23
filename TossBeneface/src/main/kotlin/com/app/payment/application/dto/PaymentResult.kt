package com.app.payment.application.dto

/**
 * 결제 승인 결과의 Sealed Class (성공/실패 분리)
 */
sealed class PaymentResult {
    data class Success(val result: ConfirmedPaymentResult) : PaymentResult()
    data class Failure(val errorCode: String, val message: String) : PaymentResult()
}
