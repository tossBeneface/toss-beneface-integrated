package com.app.api.member.dto

import com.app.domain.member.constant.Role
import io.swagger.v3.oas.annotations.media.Schema

class ChangeMemberAuthorityDto {

    data class Request(
        @field:Schema(description = "변경할 권한", example = "user/owner/admin", required = true)
        val role: String
    )

    data class Response(
        @field:Schema(description = "MemberId", example = "1", required = true)
        val memberId: Long,

        @field:Schema(description = "변경된 권한", example = "ADMIN", required = true)
        val role: Role
    )
}
