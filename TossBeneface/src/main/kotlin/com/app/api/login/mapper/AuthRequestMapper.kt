package com.app.api.login.mapper

import com.app.api.login.dto.JoinDto
import com.app.api.login.dto.LoginDto
import com.app.auth.application.dto.JoinCommand
import com.app.auth.application.dto.LoginCommand
import org.springframework.stereotype.Component

@Component
class AuthRequestMapper {

    fun toCommand(request: LoginDto.Request): LoginCommand {
        return LoginCommand(
            email = request.email,
            password = request.password
        )
    }

    fun toCommand(request: JoinDto.Request): JoinCommand {
        return JoinCommand(
            email = request.email,
            password = request.password,
            memberName = request.memberName,
            phoneNumber = request.phoneNumber,
            gender = request.gender,
            profileImg = request.profileImg,
            role = request.role
        )
    }
}
