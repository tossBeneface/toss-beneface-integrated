package com.app.auth.application.port

import java.time.Duration

interface RefreshTokenStore {
    fun save(memberId: Long, refreshToken: String, ttl: Duration)
    fun findMemberIdByToken(refreshToken: String): Long?
    fun delete(refreshToken: String)
    fun deleteByMemberId(memberId: Long)
}
