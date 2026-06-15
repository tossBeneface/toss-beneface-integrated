package com.app.api.member.mapper

import com.app.api.member.dto.MemberInfoResponseDto
import com.app.member.application.dto.MemberInfoResult
import org.springframework.stereotype.Component

@Component
class MemberInfoResponseMapper {

    fun toResponse(result: MemberInfoResult): MemberInfoResponseDto {
        return MemberInfoResponseDto(
            memberId = result.memberId,
            email = result.email,
            memberName = result.memberName,
            phoneNumber = result.phoneNumber,
            gender = result.gender,
            budget = result.budget,
            profileImg = result.profileImg,
            role = result.role
        )
    }
}
