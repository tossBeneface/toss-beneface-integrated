package com.app.api.member.dto

import com.app.domain.member.entity.Member
import io.swagger.v3.oas.annotations.media.Schema

data class MemberInfoResponseDto(
    @field:Schema(description = "MemberId", example = "1", required = true)
    val memberId: String,

    @field:Schema(description = "이메일", example = "a061283@aivle.kt.co.kr", required = true)
    val email: String,

    @field:Schema(description = "이름", example = "1", required = true)
    val memberName: String,

    @field:Schema(description = "휴대폰 번호", example = "010-0000-0000", required = true)
    val phoneNumber: String,

    @field:Schema(description = "성별", example = "male/female", required = true)
    val gender: String,

    @field:Schema(description = "잔액", example = "10000원", required = true)
    val budget: Int,

    @field:Schema(description = "프로필 사진", example = "없을시 기본 이미지로 대체", required = false)
    val profileImg: String? = null,

    @field:Schema(description = "일반 사용자/관리자 여부", example = "관리자", required = true)
    val role: String
) {
    companion object {
        fun of(member: Member): MemberInfoResponseDto {
            return MemberInfoResponseDto(
                memberId = member.memberId.toString(),
                memberName = member.memberName,
                email = member.email,
                phoneNumber = member.phoneNumber,
                gender = member.gender.toString(),
                budget = member.budget ?: 0,
                profileImg = member.profileImg,
                role = member.role.toString()
            )
        }
    }
}
