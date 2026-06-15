package com.app.member.application.usecase

import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.entity.Member
import com.app.domain.member.model.MemberAuthority
import com.app.domain.member.model.MemberIdentity
import com.app.domain.member.model.MemberOnboarding
import com.app.domain.member.model.MemberProfile
import com.app.domain.member.service.MemberService
import com.app.member.application.dto.RegisterMemberCommand
import com.app.member.application.dto.RegisteredMemberResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.Locale

@Service
@Transactional
class RegisterMemberUseCase(
    private val memberService: MemberService
) {

    fun register(command: RegisterMemberCommand): RegisteredMemberResult {
        val member = Member.registerLocal(
            identity = MemberIdentity.local(
                email = command.identity.email,
                encodedPassword = command.identity.encodedPassword
            ),
            profile = MemberProfile(
                memberName = command.profile.memberName,
                phoneNumber = command.profile.phoneNumber,
                gender = Gender.from(command.profile.gender.uppercase(Locale.ROOT)),
                profileImg = command.profile.profileImg ?: ""
            ),
            authority = MemberAuthority(Role.from(command.authority.role)),
            onboarding = MemberOnboarding(
                initialBudget = command.onboarding.initialBudget,
                status = MemberStatus.from(command.onboarding.status.uppercase(Locale.ROOT))
            )
        )

        return RegisteredMemberResult.from(memberService.registerMember(member))
    }
}
