package com.app.api.member.dto

import com.app.domain.member.constant.MemberStatus
import io.swagger.v3.oas.annotations.media.Schema

class UpdateMemberInitialStateDto {

    data class Request(
        @field:Schema(description = "초기 예산", example = "10000000", required = true)
        val initialBudget: Int,

        @field:Schema(description = "회원 상태", example = "ACTIVATE", required = true)
        val memberStatus: String
    )

    data class Response(
        @field:Schema(description = "MemberId", example = "1", required = true)
        val memberId: Long,

        @field:Schema(description = "예산", example = "10000000", required = true)
        val budget: Int,

        @field:Schema(description = "회원 상태", example = "ACTIVATE", required = true)
        val memberStatus: MemberStatus
    )
}
