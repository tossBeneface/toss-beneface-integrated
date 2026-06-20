package com.app.member.application.usecase

import com.app.domain.member.service.MemberService
import com.app.member.application.dto.CompleteMemberOnboardingCommand
import com.app.member.application.dto.MemberOnboardingProgressResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class CompleteMemberOnboardingUseCase(
    private val memberService: MemberService
) {

    fun complete(command: CompleteMemberOnboardingCommand): MemberOnboardingProgressResult {
        val member = memberService.findMemberById(command.memberId)
        member.completeOnboarding()
        return MemberOnboardingProgressResult.from(memberService.updateMember(member))
    }
}
