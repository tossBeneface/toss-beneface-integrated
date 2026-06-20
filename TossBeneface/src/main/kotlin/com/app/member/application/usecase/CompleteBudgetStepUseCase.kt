package com.app.member.application.usecase

import com.app.domain.member.constant.OnboardingStep
import com.app.domain.member.service.MemberService
import com.app.member.application.dto.CompleteMemberOnboardingStepCommand
import com.app.member.application.dto.MemberOnboardingProgressResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CompleteBudgetStepUseCase(
    private val memberService: MemberService
) {

    fun complete(command: CompleteMemberOnboardingStepCommand): MemberOnboardingProgressResult {
        val member = memberService.findMemberById(command.memberId)
        member.completeOnboardingStep(OnboardingStep.BUDGET)
        return MemberOnboardingProgressResult.from(memberService.updateMember(member))
    }
}
