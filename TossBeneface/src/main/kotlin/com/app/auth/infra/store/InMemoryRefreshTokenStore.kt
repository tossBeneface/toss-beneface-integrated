package com.app.auth.infra.store

import com.app.auth.application.port.RefreshTokenStore
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.ConcurrentHashMap

@Component
@Profile("test")
class InMemoryRefreshTokenStore : RefreshTokenStore {

    private val tokenToMember = ConcurrentHashMap<String, Long>()
    private val memberToToken = ConcurrentHashMap<Long, String>()

    override fun save(memberId: Long, refreshToken: String, ttl: Duration) {
        val existingToken = memberToToken.put(memberId, refreshToken)
        if (existingToken != null) {
            tokenToMember.remove(existingToken)
        }
        tokenToMember[refreshToken] = memberId
    }

    override fun findMemberIdByToken(refreshToken: String): Long? = tokenToMember[refreshToken]

    override fun delete(refreshToken: String) {
        val memberId = tokenToMember.remove(refreshToken)
        if (memberId != null) {
            memberToToken.remove(memberId)
        }
    }

    override fun deleteByMemberId(memberId: Long) {
        val refreshToken = memberToToken.remove(memberId)
        if (refreshToken != null) {
            tokenToMember.remove(refreshToken)
        }
    }
}
