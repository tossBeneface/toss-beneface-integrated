package com.app.domain.qr.repository

import com.app.domain.qr.entity.QrAuth
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface QrAuthRepository : JpaRepository<QrAuth, Long> {
    fun findByMemberMemberIdAndNonce(memberId: Long, nonce: String): Optional<QrAuth>
}
