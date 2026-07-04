package com.app.member.application.usecase

import com.app.domain.member.constant.Gender
import com.app.domain.member.model.MemberProfile
import com.app.domain.member.service.MemberService
import com.app.member.application.dto.MemberProfileResult
import com.app.member.application.dto.UpdateMemberProfileCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Locale

@Service
@Transactional
class UpdateMemberProfileUseCase(
    private val memberService: MemberService
) {

    fun update(command: UpdateMemberProfileCommand): MemberProfileResult {
        val member = memberService.findMemberById(command.memberId)
        member.updateProfile(
            MemberProfile(
                memberName = command.memberName,
                phoneNumber = command.phoneNumber,
                gender = Gender.from(command.gender.uppercase(Locale.ROOT)),
                profileImg = command.profileImg
            )
        )
        return MemberProfileResult.from(memberService.updateMember(member))
    }
}
