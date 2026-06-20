package com.app.member.application.usecase

import com.app.domain.member.service.MemberService
import com.app.member.application.dto.MemberOnboardingProgressResult
import com.app.member.application.dto.StartMemberOnboardingCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class StartMemberOnboardingUseCase(
    private val memberService: MemberService
) {

    fun start(command: StartMemberOnboardingCommand): MemberOnboardingProgressResult {
        val member = memberService.findMemberById(command.memberId)
        member.startOnboarding()
        return MemberOnboardingProgressResult.from(memberService.updateMember(member))
    }
}
