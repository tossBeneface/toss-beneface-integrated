package com.app.auth.application.usecase

import com.app.auth.application.dto.AuthSessionResult
import com.app.auth.application.dto.JoinCommand
import com.app.auth.application.service.TokenManager
import com.app.member.application.dto.RegisterMemberAuthority
import com.app.member.application.dto.RegisterMemberCommand
import com.app.member.application.dto.RegisterMemberIdentity
import com.app.member.application.dto.RegisterMemberProfile
import com.app.member.application.usecase.RegisterMemberUseCase
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class JoinUseCase(
    private val registerMemberUseCase: RegisterMemberUseCase,
    private val tokenManager: TokenManager,
    private val bCryptPasswordEncoder: BCryptPasswordEncoder
) {

    fun join(command: JoinCommand): AuthSessionResult {
        val registeredMember = registerMemberUseCase.register(
            RegisterMemberCommand(
                identity = RegisterMemberIdentity(
                    email = command.email,
                    encodedPassword = bCryptPasswordEncoder.encode(command.password)
                ),
                profile = RegisterMemberProfile(
                    memberName = command.memberName,
                    phoneNumber = command.phoneNumber,
                    gender = command.gender,
                    profileImg = command.profileImg
                ),
                authority = RegisterMemberAuthority(role = command.role)
            )
        )

        val jwtTokenDto = tokenManager.createJwtTokenDto(registeredMember.memberId, registeredMember.role)
        return AuthSessionResult(
            memberId = jwtTokenDto.memberId,
            email = registeredMember.email,
            memberName = registeredMember.memberName,
            phoneNumber = registeredMember.phoneNumber,
            gender = registeredMember.gender.toString(),
            budget = registeredMember.budget.toString(),
            profileImg = registeredMember.profileImg.takeUnless { it.isNullOrEmpty() },
            role = registeredMember.role.toString(),
            grantType = jwtTokenDto.grantType ?: "Bearer",
            accessToken = jwtTokenDto.accessToken,
            accessTokenExpireTime = jwtTokenDto.accessTokenExpireTime,
            refreshToken = jwtTokenDto.refreshToken,
            refreshTokenExpireTime = jwtTokenDto.refreshTokenExpireTime
        )
    }
}
