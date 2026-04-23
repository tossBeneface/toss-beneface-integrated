package com.app.auth.application.usecase

import com.app.api.login.dto.LoginDto
import com.app.domain.member.service.MemberService
import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import com.app.global.jwt.service.TokenManager
import jakarta.servlet.http.HttpServletResponse
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
    fun login(request: LoginDto.Request, response: HttpServletResponse): LoginDto.Response {
        val member = memberService.findMemberByEmail(request.email)
            .orElseThrow { BusinessException(ErrorCode.INVALID_EMAIL) }

        val encodedPassword = member.password
        if (encodedPassword.isNullOrBlank() || !bCryptPasswordEncoder.matches(request.password, encodedPassword)) {
            throw BusinessException(ErrorCode.INVALID_PASSWORD)
        }

        val memberId = member.memberId ?: throw IllegalStateException("Member id is missing")

        val jwtTokenDto = tokenManager.createJwtTokenDto(memberId, member.role, response)
        val loginResponse = LoginDto.Response.of(jwtTokenDto)
        loginResponse.email = member.email
        loginResponse.memberName = member.memberName
        loginResponse.phoneNumber = member.phoneNumber
        loginResponse.gender = member.gender.toString()
        loginResponse.role = member.role.toString()
        if (!member.profileImg.isNullOrEmpty()) {
            loginResponse.profileImg = member.profileImg
        }

        return loginResponse
    }
}
