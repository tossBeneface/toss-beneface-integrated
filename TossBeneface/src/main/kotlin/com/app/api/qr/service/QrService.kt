package com.app.api.qr.service

import com.app.domain.member.repository.MemberRepository
import com.app.domain.qr.entity.QrAuth
import com.app.domain.qr.repository.QrAuthRepository
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.client.j2se.MatrixToImageWriter
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.io.ByteArrayOutputStream
import java.security.SecureRandom
import java.time.Duration
import java.time.LocalDateTime
import java.util.*

@Service
@Transactional(readOnly = true)
class QrService(
    private val qrAuthRepository: QrAuthRepository,
    private val memberRepository: MemberRepository
) {
    private val secureRandom = SecureRandom()

    @Transactional
    fun generateQr(memberId: Long): ByteArray {
        val member = memberRepository.findById(memberId)
            .orElseThrow { RuntimeException("Member not found") }

        val nonce = generateNonce()
        val qrAuth = QrAuth(
            nonce = nonce,
            authenticated = false,
            member = member
        )
        qrAuthRepository.save(qrAuth)

        val text = "${member.memberId}|$nonce"
        val width = 400
        val height = 400

        val bitMatrix = MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height)
        
        ByteArrayOutputStream().use { out ->
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", out)
            return out.toByteArray()
        }
    }

    @Transactional
    fun authenticateQr(memberId: Long, nonce: String): String {
        val qrAuthOptional = qrAuthRepository.findByMemberMemberIdAndNonce(memberId, nonce)

        if (qrAuthOptional.isEmpty) {
            return "FAIL_INVALID"
        }

        val qrAuth = qrAuthOptional.get()

        if (qrAuth.authenticated) {
            return "FAIL_ALREADY_USED"
        }

        val createdAt = qrAuth.createdAt
        if (Duration.between(createdAt, LocalDateTime.now()).seconds > 300) {
            return "FAIL_EXPIRED"
        }

        qrAuth.authenticated = true
        qrAuthRepository.save(qrAuth)
        return "SUCCESS"
    }

    private fun generateNonce(): String {
        val randomBytes = ByteArray(8)
        secureRandom.nextBytes(randomBytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes)
    }
}
