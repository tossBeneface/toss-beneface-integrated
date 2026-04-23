package com.app.api.login.dto

import com.app.global.jwt.dto.JwtTokenDto
import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

class LoginDto {

    data class Request(
        @field:Schema(description = "이메일", example = "test@example.com", required = true)
        val email: String,

        @field:Schema(description = "비밀번호", example = "password123", required = true)
        val password: String
    )

    data class Response(
        @field:Schema(description = "MemberId", example = "1", required = true)
        var memberId: String? = null,

        @field:Schema(description = "이메일", example = "a061283@aivle.kt.co.kr", required = true)
        var email: String? = null,

        @field:Schema(description = "이름", example = "1", required = true)
        var memberName: String? = null,

        @field:Schema(description = "휴대폰 번호", example = "010-0000-0000", required = true)
        var phoneNumber: String? = null,

        @field:Schema(description = "성별", example = "male/female", required = true)
        var gender: String? = null,

        @field:Schema(description = "잔액", example = "10000원", required = true)
        var budget: String? = null,

        @field:Schema(description = "프로필 사진", example = "없을시 기본 이미지로 대체", required = false)
        var profileImg: String? = null,

        @field:Schema(description = "일반 사용자/관리자 여부", example = "관리자", required = true)
        var role: String? = null,

        @field:Schema(description = "grantType", example = "Bearer", required = true)
        var grantType: String? = null,

        @field:Schema(description = "accessToken", example = "...", required = true)
        var accessToken: String? = null,

        @field:Schema(description = "access token 만료 시간", example = "2024-03-23 23:18:14", required = true)
        @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        var accessTokenExpireTime: Date? = null,

        @field:Schema(description = "refreshToken", example = "...")
        var refreshToken: String? = null,

        @field:Schema(description = "refresh token 만료 시간", example = "2024-04-06 23:03:14", required = true)
        @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        var refreshTokenExpireTime: Date? = null
    ) {
        companion object {
            fun of(jwtTokenDto: JwtTokenDto): Response {
                return Response(
                    memberId = jwtTokenDto.memberId,
                    grantType = jwtTokenDto.grantType,
                    accessToken = jwtTokenDto.accessToken,
                    accessTokenExpireTime = jwtTokenDto.accessTokenExpireTime,
                    refreshToken = jwtTokenDto.refreshToken,
                    refreshTokenExpireTime = jwtTokenDto.refreshTokenExpireTime
                )
            }
        }
    }
}
