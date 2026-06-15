package com.app.api.payment.controller

import com.app.global.resolver.memberInfo.MemberInfo
import com.app.global.resolver.memberInfo.MemberInfoDto
import com.app.payment.application.PaymentConfirmType
import com.app.payment.application.dto.*
import com.app.payment.application.usecase.*
import jakarta.servlet.http.HttpServletRequest
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.*

@Controller
@RequestMapping("/api/payment")
class PaymentController(
    private val confirmPaymentUseCase: ConfirmPaymentUseCase,
    private val confirmBillingUseCase: ConfirmBillingUseCase,
    private val issueBillingKeyUseCase: IssueBillingKeyUseCase,
    private val callbackAuthUseCase: CallbackAuthUseCase,
    private val confirmBrandpayUseCase: ConfirmBrandpayUseCase
) {

    @PostMapping("/confirm/widget")
    @Throws(Exception::class)
    fun confirmWidgetPayment(
        @MemberInfo memberInfoDto: MemberInfoDto,
        @Valid @RequestBody command: ConfirmPaymentCommand
    ): ResponseEntity<Map<String, Any>> {
        val result = confirmPaymentUseCase.confirm(
            command.withMemberId(memberInfoDto.memberId),
            PaymentConfirmType.WIDGET
        )
        return toResponseEntity(result)
    }

    @PostMapping("/confirm/payment")
    @Throws(Exception::class)
    fun confirmPayment(
        @MemberInfo memberInfoDto: MemberInfoDto,
        @Valid @RequestBody command: ConfirmPaymentCommand
    ): ResponseEntity<Map<String, Any>> {
        val result = confirmPaymentUseCase.confirm(
            command.withMemberId(memberInfoDto.memberId),
            PaymentConfirmType.PAYMENT
        )
        return toResponseEntity(result)
    }

    @PostMapping("/confirm-billing")
    @Throws(Exception::class)
    fun confirmBilling(
        @Valid @RequestBody command: ConfirmBillingCommand
    ): ResponseEntity<Map<String, Any>> {
        return toResponseEntity(confirmBillingUseCase.confirm(command))
    }

    @PostMapping("/issue-billing-key")
    @Throws(Exception::class)
    fun issueBillingKey(
        @Valid @RequestBody command: IssueBillingKeyCommand
    ): ResponseEntity<Map<String, Any>> {
        return toResponseEntity(issueBillingKeyUseCase.issue(command))
    }

    @GetMapping("/callback-auth")
    @Throws(Exception::class)
    fun callbackAuth(
        @RequestParam customerKey: String,
        @RequestParam code: String
    ): ResponseEntity<Map<String, Any>> {
        return toResponseEntity(callbackAuthUseCase.requestAccessToken(CallbackAuthCommand(customerKey, code)))
    }

    @PostMapping("/confirm/brandpay", consumes = ["application/json"])
    @Throws(Exception::class)
    fun confirmBrandpay(
        @Valid @RequestBody command: ConfirmBrandpayCommand
    ): ResponseEntity<Map<String, Any>> {
        return toResponseEntity(confirmBrandpayUseCase.confirm(command))
    }

    private fun toResponseEntity(result: PaymentResult): ResponseEntity<Map<String, Any>> {
        return when (result) {
            is PaymentResult.Success -> {
                val confirmed = result.result
                ResponseEntity.ok(
                    mapOf(
                        "status" to confirmed.status.name,
                        "paymentKey" to confirmed.paymentKey,
                        "orderId" to confirmed.orderId,
                        "totalAmount" to confirmed.totalAmount
                    )
                )
            }
            is PaymentResult.Failure -> {
                ResponseEntity.status(400).body(
                    mapOf(
                        "errorCode" to result.errorCode,
                        "errorMessage" to result.message
                    )
                )
            }
        }
    }

    private fun toResponseEntity(response: TossApiResponse): ResponseEntity<Map<String, Any>> {
        return ResponseEntity.status(response.statusCode).body(response.body)
    }

    @GetMapping("/")
    fun index(): String {
        return "/widget/checkout"
    }

    @GetMapping("/fail")
    fun failPayment(request: HttpServletRequest, model: Model): String {
        model.addAttribute("code", request.getParameter("code"))
        model.addAttribute("message", request.getParameter("message"))
        return "/fail"
    }
}
