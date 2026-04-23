package com.app.global.util

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

@Component
class CookieEncryptionUtils(
    @Value("\${cookie.encryption.secret}") secretKey: String
) {
    private val secretKeyBytes: ByteArray

    init {
        val candidate = secretKey.toByteArray(StandardCharsets.UTF_8)
        val keyLength = candidate.size
        if (keyLength != 16 && keyLength != 24 && keyLength != 32) {
            throw IllegalStateException("cookie.encryption.secret must be 16, 24, or 32 bytes long")
        }
        this.secretKeyBytes = candidate
    }

    fun encrypt(value: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val keySpec = SecretKeySpec(secretKeyBytes, ALGORITHM)
        val ivSpec = IvParameterSpec(IV)
        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec)
        val encrypted = cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
    }

    fun decrypt(encryptedValue: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val keySpec = SecretKeySpec(secretKeyBytes, ALGORITHM)
        val ivSpec = IvParameterSpec(IV)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        val original = cipher.doFinal(Base64.getDecoder().decode(encryptedValue))
        return String(original, StandardCharsets.UTF_8)
    }

    companion object {
        private const val ALGORITHM = "AES"
        private const val TRANSFORMATION = "AES/CBC/PKCS5Padding"
        private val IV = ByteArray(16)
    }
}
