package com.app.api.member.dto

import com.app.domain.member.constant.Gender
import io.swagger.v3.oas.annotations.media.Schema

class UpdateMemberProfileDto {

    data class Request(
        @field:Schema(description = "이름", example = "김에쁠", required = true)
        val memberName: String,

        @field:Schema(description = "휴대폰 번호", example = "010-0000-0000", required = true)
        val phoneNumber: String,

        @field:Schema(description = "성별", example = "male/female", required = true)
        val gender: String,

        @field:Schema(description = "프로필 사진", example = "없을시 기본 이미지로 대체", required = false)
        val profileImg: String? = null
    )

    data class Response(
        @field:Schema(description = "MemberId", example = "1", required = true)
        val memberId: Long,

        @field:Schema(description = "이름", example = "김에쁠", required = true)
        val memberName: String,

        @field:Schema(description = "휴대폰 번호", example = "010-0000-0000", required = true)
        val phoneNumber: String,

        @field:Schema(description = "성별", example = "MALE/FEMALE", required = true)
        val gender: Gender,

        @field:Schema(description = "프로필 사진", example = "없을시 기본 이미지로 대체", required = false)
        val profileImg: String? = null
    )
}
