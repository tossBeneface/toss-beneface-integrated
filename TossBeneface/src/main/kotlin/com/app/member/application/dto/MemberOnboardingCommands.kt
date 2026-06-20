package com.app.member.application.dto

import com.app.domain.member.constant.OnboardingStatus
import com.app.domain.member.constant.OnboardingStep
import com.app.domain.member.entity.Member
import java.time.LocalDateTime

data class StartMemberOnboardingCommand(
    val memberId: Long
)

data class CompleteMemberOnboardingStepCommand(
    val memberId: Long
)

data class CompleteMemberOnboardingCommand(
    val memberId: Long
)

data class MemberOnboardingProgressResult(
    val memberId: Long,
    val onboardingStatus: OnboardingStatus,
    val onboardingStep: OnboardingStep,
    val onboardingCompletedAt: LocalDateTime?
) {
    companion object {
        fun from(member: Member): MemberOnboardingProgressResult {
            return MemberOnboardingProgressResult(
                memberId = requireNotNull(member.memberId) { "Member id is missing" },
                onboardingStatus = member.onboardingStatus,
                onboardingStep = member.onboardingStep,
                onboardingCompletedAt = member.onboardingCompletedAt
            )
        }
    }
}
