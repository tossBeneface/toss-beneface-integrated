package com.app.api.payment.controller

import com.app.api.payment.dto.PaymentResponse
import com.app.api.payment.mapper.PaymentResponseMapper
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
    private val confirmBrandpayUseCase: ConfirmBrandpayUseCase,
    private val paymentResponseMapper: PaymentResponseMapper
) {

    @PostMapping("/confirm/widget")
    @Throws(Exception::class)
    fun confirmWidgetPayment(
        @MemberInfo memberInfoDto: MemberInfoDto,
        @Valid @RequestBody command: ConfirmPaymentCommand
    ): ResponseEntity<PaymentResponse> {
        val result = confirmPaymentUseCase.confirm(
            command.withMemberId(memberInfoDto.memberId),
            PaymentConfirmType.WIDGET
        )
        return paymentResponseMapper.toResponseEntity(result)
    }

    @PostMapping("/confirm/payment")
    @Throws(Exception::class)
    fun confirmPayment(
        @MemberInfo memberInfoDto: MemberInfoDto,
        @Valid @RequestBody command: ConfirmPaymentCommand
    ): ResponseEntity<PaymentResponse> {
        val result = confirmPaymentUseCase.confirm(
            command.withMemberId(memberInfoDto.memberId),
            PaymentConfirmType.PAYMENT
        )
        return paymentResponseMapper.toResponseEntity(result)
    }

    @PostMapping("/confirm-billing")
    @Throws(Exception::class)
    fun confirmBilling(
        @Valid @RequestBody command: ConfirmBillingCommand
    ): ResponseEntity<PaymentResponse> {
        return paymentResponseMapper.toResponseEntity(confirmBillingUseCase.confirm(command))
    }

    @PostMapping("/issue-billing-key")
    @Throws(Exception::class)
    fun issueBillingKey(
        @Valid @RequestBody command: IssueBillingKeyCommand
    ): ResponseEntity<PaymentResponse> {
        return paymentResponseMapper.toResponseEntity(issueBillingKeyUseCase.issue(command))
    }

    @GetMapping("/callback-auth")
    @Throws(Exception::class)
    fun callbackAuth(
        @RequestParam customerKey: String,
        @RequestParam code: String
    ): ResponseEntity<PaymentResponse> {
        return paymentResponseMapper.toResponseEntity(callbackAuthUseCase.requestAccessToken(CallbackAuthCommand(customerKey, code)))
    }

    @PostMapping("/confirm/brandpay", consumes = ["application/json"])
    @Throws(Exception::class)
    fun confirmBrandpay(
        @Valid @RequestBody command: ConfirmBrandpayCommand
    ): ResponseEntity<PaymentResponse> {
        return paymentResponseMapper.toResponseEntity(confirmBrandpayUseCase.confirm(command))
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
