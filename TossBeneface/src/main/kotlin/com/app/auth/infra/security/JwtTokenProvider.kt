package com.app.auth.infra.security

import com.app.domain.member.constant.Role
import com.app.global.error.ErrorCode
import com.app.global.error.exception.AuthenticationException
import com.app.global.jwt.constant.TokenType
import io.jsonwebtoken.Claims
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.Key
import java.util.Date

@Component
class JwtTokenProvider(
    @Value("\${token.secret}") tokenSecret: String
) {
    private val key: Key = Keys.hmacShaKeyFor(tokenSecret.toByteArray(StandardCharsets.UTF_8))

    fun createAccessToken(memberId: Long, role: Role, expirationTime: Date): String {
        return Jwts.builder()
            .setHeaderParam("type", "JWT")
            .setSubject(TokenType.ACCESS.name)
            .setIssuedAt(Date())
            .setExpiration(expirationTime)
            .claim("memberId", memberId)
            .claim("role", role.name)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun createRefreshToken(memberId: Long, expirationTime: Date): String {
        return Jwts.builder()
            .setHeaderParam("type", "JWT")
            .setSubject(TokenType.REFRESH.name)
            .setIssuedAt(Date())
            .setExpiration(expirationTime)
            .claim("memberId", memberId)
            .signWith(key, SignatureAlgorithm.HS512)
            .compact()
    }

    fun parseAccessToken(token: String): Claims {
        val claims = parseClaims(token)
        if (!TokenType.isAccessToken(claims.subject)) {
            throw AuthenticationException(ErrorCode.NOT_ACCESS_TOKEN_TYPE)
        }
        return claims
    }

    fun parseRefreshToken(token: String): Claims {
        val claims = parseClaims(token)
        if (claims.subject != TokenType.REFRESH.name) {
            throw AuthenticationException(ErrorCode.NOT_VALID_TOKEN)
        }
        return claims
    }

    fun extractMemberId(claims: Claims): Long {
        val rawMemberId = claims["memberId"] ?: throw AuthenticationException(ErrorCode.NOT_VALID_TOKEN)
        return when (rawMemberId) {
            is Number -> rawMemberId.toLong()
            is String -> rawMemberId.toLongOrNull() ?: throw AuthenticationException(ErrorCode.NOT_VALID_TOKEN)
            else -> throw AuthenticationException(ErrorCode.NOT_VALID_TOKEN)
        }
    }

    fun extractRole(claims: Claims): Role {
        val rawRole = claims["role"]?.toString() ?: throw AuthenticationException(ErrorCode.NOT_VALID_TOKEN)
        return try {
            Role.valueOf(rawRole.uppercase())
        } catch (e: IllegalArgumentException) {
            throw AuthenticationException(ErrorCode.NOT_VALID_TOKEN)
        }
    }

    private fun parseClaims(token: String): Claims {
        try {
            return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .body
        } catch (e: ExpiredJwtException) {
            throw AuthenticationException(ErrorCode.TOKEN_EXPIRED)
        } catch (e: AuthenticationException) {
            throw e
        } catch (e: Exception) {
            throw AuthenticationException(ErrorCode.NOT_VALID_TOKEN)
        }
    }
}
