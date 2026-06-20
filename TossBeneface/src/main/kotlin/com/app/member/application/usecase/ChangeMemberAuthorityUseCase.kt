package com.app.member.application.usecase

import com.app.domain.member.constant.Role
import com.app.domain.member.model.MemberAuthority
import com.app.domain.member.service.MemberService
import com.app.member.application.dto.ChangeMemberAuthorityCommand
import com.app.member.application.dto.MemberAuthorityResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ChangeMemberAuthorityUseCase(
    private val memberService: MemberService
) {

    fun change(command: ChangeMemberAuthorityCommand): MemberAuthorityResult {
        val member = memberService.findMemberById(command.memberId)
        member.changeAuthority(MemberAuthority(Role.from(command.role)))
        return MemberAuthorityResult.from(memberService.updateMember(member))
    }
}
