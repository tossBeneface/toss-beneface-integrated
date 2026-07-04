package com.app.auth.application.usecase

import com.app.auth.application.dto.AuthSessionResult
import com.app.auth.application.dto.LoginCommand
import com.app.domain.member.service.MemberService
import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import com.app.auth.application.service.TokenManager
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class LoginUseCase(
    private val memberService: MemberService,
    private val tokenManager: TokenManager,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder
    ) {

    @Transactional
    fun login(command: LoginCommand): AuthSessionResult {
        val member = memberService.findMemberByEmail(command.email)
            .orElseThrow { BusinessException(ErrorCode.INVALID_EMAIL) }

        val encodedPassword = member.password
        if (encodedPassword.isNullOrBlank() || !bCryptPasswordEncoder.matches(command.password, encodedPassword)) {
            throw BusinessException(ErrorCode.INVALID_PASSWORD)
        }

        val memberId = member.memberId ?: throw IllegalStateException("Member id is missing")

        val jwtTokenDto = tokenManager.createJwtTokenDto(memberId, member.role)
        return AuthSessionResult(
            memberId = jwtTokenDto.memberId,
            email = member.email,
            memberName = member.memberName,
            phoneNumber = member.phoneNumber,
            gender = member.gender.toString(),
            profileImg = member.profileImg.takeUnless { it.isNullOrEmpty() },
            role = member.role.toString(),
            grantType = jwtTokenDto.grantType ?: "Bearer",
            accessToken = jwtTokenDto.accessToken,
            accessTokenExpireTime = jwtTokenDto.accessTokenExpireTime,
            refreshToken = jwtTokenDto.refreshToken,
            refreshTokenExpireTime = jwtTokenDto.refreshTokenExpireTime
        )
    }
}
