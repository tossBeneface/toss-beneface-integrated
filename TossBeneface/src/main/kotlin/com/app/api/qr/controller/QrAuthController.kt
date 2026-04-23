package com.app.api.qr.controller

import com.app.api.qr.service.QrService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/qr")
class QrAuthController(
    private val qrService: QrService
) {

    @GetMapping("/authenticate")
    fun authenticateQR(
        @RequestParam memberId: Long,
        @RequestParam nonce: String
    ): ResponseEntity<*> {
        val result = qrService.authenticateQr(memberId, nonce)

        return when (result) {
            "SUCCESS" -> ResponseEntity.ok("인증 성공! 다음 페이지로 이동 가능합니다.")
            "FAIL_INVALID" -> ResponseEntity.status(401).body("인증 실패! 올바르지 않은 QR 코드입니다.")
            "FAIL_ALREADY_USED" -> ResponseEntity.status(403).body("인증 실패! 이미 사용된 QR 코드입니다.")
            "FAIL_EXPIRED" -> ResponseEntity.status(401).body("인증 실패! QR 코드가 만료되었습니다.")
            else -> ResponseEntity.status(500).body("서버 오류가 발생했습니다.")
        }
    }
}
