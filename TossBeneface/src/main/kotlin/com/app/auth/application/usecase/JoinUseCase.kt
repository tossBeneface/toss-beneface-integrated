package com.app.auth.application.usecase

import com.app.api.login.dto.JoinDto
import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.service.MemberService
import com.app.global.jwt.dto.JwtTokenDto
import com.app.global.jwt.service.TokenManager
import jakarta.servlet.http.HttpServletResponse
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

    fun join(request: JoinDto.Request, response: HttpServletResponse): JoinDto.Response {
        val member = Member(
            email = request.email,
            password = bCryptPasswordEncoder.encode(request.password),
            memberName = request.memberName,
            phoneNumber = request.phoneNumber,
            gender = Gender.valueOf(request.gender.uppercase(Locale.ROOT)),
            profileImg = request.profileImg ?: "",
            budget = 10_000_000,
            role = Role.from(request.role),
            memberStatus = MemberStatus.ACTIVATE
        )

        val savedMember = memberService.registerMember(member)
        val memberId = savedMember.memberId ?: throw IllegalStateException("Member id is missing")

        val jwtTokenDto = tokenManager.createJwtTokenDto(memberId, savedMember.role, response)
        return JoinDto.Response.of(jwtTokenDto, request)
    }
}
