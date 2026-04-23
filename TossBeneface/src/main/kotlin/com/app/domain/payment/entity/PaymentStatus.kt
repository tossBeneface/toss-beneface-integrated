package com.app.domain.payment.entity

/**
 * 결제 상태 정의
 * READY: 결제 생성됨 (승인 전)
 * DONE: 결제 완료
 * CANCELED: 결제 취소됨
 * FAILED: 결제 실패
 * EXPIRED: 결제 유효시간 만료
 */
enum class PaymentStatus {
    READY,
    DONE,
    CANCELED,
    FAILED,
    EXPIRED;

    companion object {
        fun from(status: String?): PaymentStatus {
            return entries.find { it.name.equals(status, ignoreCase = true) } ?: READY
        }
    }
}
