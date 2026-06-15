package com.app.auth.application.usecase

import com.app.auth.application.dto.AuthSessionResult
import com.app.auth.application.dto.JoinCommand
import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.service.MemberService
import com.app.global.jwt.service.TokenManager
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class JoinUseCase(
    private val memberService: MemberService,
    private val tokenManager: TokenManager,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder
) {

    fun join(command: JoinCommand): AuthSessionResult {
        val member = Member(
            email = command.email,
            password = bCryptPasswordEncoder.encode(command.password),
            memberName = command.memberName,
            phoneNumber = command.phoneNumber,
            gender = Gender.valueOf(command.gender.uppercase(Locale.ROOT)),
            profileImg = command.profileImg ?: "",
            budget = 10_000_000,
            role = Role.from(command.role),
            memberStatus = MemberStatus.ACTIVATE
        )

        val savedMember = memberService.registerMember(member)
        val memberId = savedMember.memberId ?: throw IllegalStateException("Member id is missing")

        val jwtTokenDto = tokenManager.createJwtTokenDto(memberId, savedMember.role)
        return AuthSessionResult(
            memberId = jwtTokenDto.memberId,
            email = command.email,
            memberName = command.memberName,
            phoneNumber = command.phoneNumber,
            gender = command.gender,
            role = command.role,
            grantType = jwtTokenDto.grantType ?: "Bearer",
            accessToken = jwtTokenDto.accessToken,
            accessTokenExpireTime = jwtTokenDto.accessTokenExpireTime,
            refreshToken = jwtTokenDto.refreshToken,
            refreshTokenExpireTime = jwtTokenDto.refreshTokenExpireTime
        )
    }
}
