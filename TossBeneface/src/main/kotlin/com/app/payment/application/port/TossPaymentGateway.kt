package com.app.payment.application.port

import com.app.payment.application.PaymentConfirmType
import com.app.payment.application.dto.*

/**
 * 토스 페이먼츠 결제 연동 포트
 */
interface TossPaymentGateway {
    /**
     * 결제 승인
     */
    fun confirmPayment(command: ConfirmPaymentCommand, confirmType: PaymentConfirmType): PaymentResult

    /**
     * 빌링 키 발급
     */
    fun issueBillingKey(command: IssueBillingKeyCommand): TossApiResponse

    /**
     * 빌링 결제 승인
     */
    fun confirmBilling(command: ConfirmBillingCommand, billingKey: String): TossApiResponse

    /**
     * 브랜드페이 액세스 토큰 요청
     */
    fun requestBrandpayAccessToken(command: CallbackAuthCommand): TossApiResponse

    /**
     * 브랜드페이 승인
     */
    fun confirmBrandpay(command: ConfirmBrandpayCommand): TossApiResponse
}
