package com.app.member.application.usecase

import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.model.MemberInitialState
import com.app.domain.member.service.MemberService
import com.app.member.application.dto.MemberInitialStateResult
import com.app.member.application.dto.UpdateMemberInitialStateCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Locale

@Service
@Transactional
class UpdateMemberInitialStateUseCase(
    private val memberService: MemberService
) {

    fun update(command: UpdateMemberInitialStateCommand): MemberInitialStateResult {
        val member = memberService.findMemberById(command.memberId)
        member.updateInitialState(
            MemberInitialState(
                initialBudget = command.initialBudget,
                memberStatus = MemberStatus.from(command.memberStatus.uppercase(Locale.ROOT))
            )
        )
        return MemberInitialStateResult.from(memberService.updateMember(member))
    }
}
