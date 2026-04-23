package com.app.api.login.dto

import com.app.global.jwt.dto.JwtTokenDto
import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

class JoinDto {

    data class Request(
        @field:Schema(description = "이메일", example = "a061283@aivle.kt.co.kr", required = true)
        val email: String,

        @field:Schema(description = "비밀번호", example = "비밀번호 정책 설명", required = true)
        val password: String,

        @field:Schema(description = "이름", example = "김에쁠", required = true)
        val memberName: String,

        @field:Schema(description = "휴대폰 번호", example = "010-0000-0000", required = true)
        val phoneNumber: String,

        @field:Schema(description = "성별", example = "male/female", required = true)
        val gender: String,

        @field:Schema(description = "프로필 사진", example = "없을시 기본 이미지로 대체", required = false)
        val profileImg: String? = null,

        @field:Schema(description = "사용자 분류", example = "user/owner/admin", required = true)
        val role: String
    )

    data class Response(
        @field:Schema(description = "MemberId", example = "1", required = true)
        val memberId: String?,

        @field:Schema(description = "이메일", example = "a061283@aivle.kt.co.kr", required = true)
        val email: String,

        @field:Schema(description = "이름", example = "1", required = true)
        val memberName: String,

        @field:Schema(description = "휴대폰 번호", example = "010-0000-0000", required = true)
        val phoneNumber: String,

        @field:Schema(description = "성별", example = "male/female", required = true)
        val gender: String,

        @field:Schema(description = "잔액", example = "10000원", required = false)
        val budget: String? = null,

        @field:Schema(description = "프로필 사진", example = "없을시 기본 이미지로 대체", required = false)
        val profileImg: String? = null,

        @field:Schema(description = "사용자 분류", example = "user/owner/admin", required = true)
        val role: String,

        @field:Schema(description = "grantType", example = "Bearer", required = true)
        val grantType: String,

        @field:Schema(description = "accessToken", example = "...", required = true)
        val accessToken: String?,

        @field:Schema(description = "access token 만료 시간", example = "2024-03-23 23:18:14", required = true)
        @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        val accessTokenExpireTime: Date?,

        @field:Schema(description = "refreshToken", example = "...")
        val refreshToken: String?,

        @field:Schema(description = "refresh token 만료 시간", example = "2024-04-06 23:03:14", required = true)
        @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
        val refreshTokenExpireTime: Date?
    ) {
        companion object {
            fun of(jwtTokenDto: JwtTokenDto, request: Request): Response {
                return Response(
                    email = request.email,
                    memberName = request.memberName,
                    phoneNumber = request.phoneNumber,
                    gender = request.gender,
                    role = request.role,
                    memberId = jwtTokenDto.memberId,
                    grantType = jwtTokenDto.grantType ?: "Bearer",
                    accessToken = jwtTokenDto.accessToken,
                    accessTokenExpireTime = jwtTokenDto.accessTokenExpireTime,
                    refreshToken = jwtTokenDto.refreshToken,
                    refreshTokenExpireTime = jwtTokenDto.refreshTokenExpireTime
                )
            }
        }
    }
}
