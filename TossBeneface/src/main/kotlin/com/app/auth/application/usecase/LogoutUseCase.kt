package com.app.auth.application.usecase

import com.app.auth.infra.security.JwtTokenProvider
import com.app.domain.member.service.MemberService
import com.app.global.jwt.service.TokenManager
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class LogoutUseCase(
    private val memberService: MemberService,
    private val tokenManager: TokenManager,
    private val jwtTokenProvider: JwtTokenProvider
) {

    fun logout(accessToken: String) {
        val claims = jwtTokenProvider.parseAccessToken(accessToken)
        val memberId = jwtTokenProvider.extractMemberId(claims)
        val member = memberService.findMemberById(memberId)

        tokenManager.destroyTokenByMemberId(member.memberId ?: throw IllegalStateException("Member id is missing"))
    }
}
