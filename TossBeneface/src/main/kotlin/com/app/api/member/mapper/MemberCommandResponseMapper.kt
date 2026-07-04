package com.app.api.member.mapper

import com.app.api.member.dto.ChangeMemberAuthorityDto
import com.app.api.member.dto.MemberOnboardingResponseDto
import com.app.api.member.dto.UpdateMemberInitialStateDto
import com.app.api.member.dto.UpdateMemberProfileDto
import com.app.member.application.dto.MemberAuthorityResult
import com.app.member.application.dto.MemberInitialStateResult
import com.app.member.application.dto.MemberOnboardingProgressResult
import com.app.member.application.dto.MemberProfileResult
import org.springframework.stereotype.Component

@Component
class MemberCommandResponseMapper {

    fun toResponse(result: MemberOnboardingProgressResult): MemberOnboardingResponseDto {
        return MemberOnboardingResponseDto(
            memberId = result.memberId,
            onboardingStatus = result.onboardingStatus,
            onboardingStep = result.onboardingStep,
            onboardingCompletedAt = result.onboardingCompletedAt
        )
    }

    fun toResponse(result: MemberProfileResult): UpdateMemberProfileDto.Response {
        return UpdateMemberProfileDto.Response(
            memberId = result.memberId,
            memberName = result.memberName,
            phoneNumber = result.phoneNumber,
            gender = result.gender,
            profileImg = result.profileImg
        )
    }

    fun toResponse(result: MemberInitialStateResult): UpdateMemberInitialStateDto.Response {
        return UpdateMemberInitialStateDto.Response(
            memberId = result.memberId,
            budget = result.budget,
            memberStatus = result.memberStatus
        )
    }

    fun toResponse(result: MemberAuthorityResult): ChangeMemberAuthorityDto.Response {
        return ChangeMemberAuthorityDto.Response(
            memberId = result.memberId,
            role = result.role
        )
    }
}
