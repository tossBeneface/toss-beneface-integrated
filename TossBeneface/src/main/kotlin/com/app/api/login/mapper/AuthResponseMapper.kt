package com.app.api.login.mapper

import com.app.api.login.dto.JoinDto
import com.app.api.login.dto.LoginDto
import com.app.auth.application.dto.AuthSessionResult
import org.springframework.stereotype.Component

@Component
class AuthResponseMapper {

    fun toLoginResponse(result: AuthSessionResult): LoginDto.Response {
        return LoginDto.Response(
            memberId = result.memberId,
            email = result.email,
            memberName = result.memberName,
            phoneNumber = result.phoneNumber,
            gender = result.gender,
            budget = result.budget,
            profileImg = result.profileImg,
            role = result.role,
            grantType = result.grantType,
            accessToken = result.accessToken,
            accessTokenExpireTime = result.accessTokenExpireTime,
            refreshToken = result.refreshToken,
            refreshTokenExpireTime = result.refreshTokenExpireTime
        )
    }

    fun toJoinResponse(result: AuthSessionResult): JoinDto.Response {
        return JoinDto.Response(
            memberId = result.memberId,
            email = result.email,
            memberName = result.memberName,
            phoneNumber = result.phoneNumber,
            gender = result.gender,
            budget = result.budget,
            profileImg = result.profileImg,
            role = result.role,
            grantType = result.grantType,
            accessToken = result.accessToken,
            accessTokenExpireTime = result.accessTokenExpireTime,
            refreshToken = result.refreshToken,
            refreshTokenExpireTime = result.refreshTokenExpireTime
        )
    }
}
