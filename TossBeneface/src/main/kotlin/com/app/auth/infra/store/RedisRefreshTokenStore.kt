package com.app.auth.infra.store

import com.app.auth.application.port.RefreshTokenStore
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
@Profile("!test")
class RedisRefreshTokenStore(
    private val redisTemplate: StringRedisTemplate
) : RefreshTokenStore {

    override fun save(memberId: Long, refreshToken: String, ttl: Duration) {
        val existingToken = redisTemplate.opsForValue().get(memberKey(memberId))
        if (existingToken != null) {
            redisTemplate.delete(tokenKey(existingToken))
        }

        redisTemplate.opsForValue().set(tokenKey(refreshToken), memberId.toString(), ttl)
        redisTemplate.opsForValue().set(memberKey(memberId), refreshToken, ttl)
    }

    override fun findMemberIdByToken(refreshToken: String): Long? {
        return redisTemplate.opsForValue().get(tokenKey(refreshToken))?.toLongOrNull()
    }

    override fun delete(refreshToken: String) {
        val memberId = findMemberIdByToken(refreshToken)
        redisTemplate.delete(tokenKey(refreshToken))
        if (memberId != null) {
            redisTemplate.delete(memberKey(memberId))
        }
    }

    override fun deleteByMemberId(memberId: Long) {
        val existingToken = redisTemplate.opsForValue().get(memberKey(memberId))
        if (existingToken != null) {
            redisTemplate.delete(tokenKey(existingToken))
        }
        redisTemplate.delete(memberKey(memberId))
    }

    private fun tokenKey(refreshToken: String): String = "auth:refresh:token:$refreshToken"

    private fun memberKey(memberId: Long): String = "auth:refresh:member:$memberId"
}
