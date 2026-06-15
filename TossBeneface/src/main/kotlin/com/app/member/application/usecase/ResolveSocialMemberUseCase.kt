package com.app.member.application.usecase

import com.app.domain.member.entity.Member
import com.app.domain.member.model.MemberIdentity
import com.app.domain.member.model.MemberProfile
import com.app.domain.member.service.MemberService
import com.app.member.application.dto.RegisteredMemberResult
import com.app.member.application.dto.ResolveSocialMemberCommand
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ResolveSocialMemberUseCase(
    private val memberService: MemberService
) {

    fun resolve(command: ResolveSocialMemberCommand): RegisteredMemberResult {
        val member = memberService.findMemberBySocialIdentity(command.socialType, command.socialId)
            .orElseGet {
                memberService.findMemberByEmail(command.email)
                    .map { existingMember ->
                        existingMember.connectSocialIdentity(
                            socialType = command.socialType,
                            socialId = command.socialId,
                            memberName = command.memberName
                        )
                        memberService.updateMember(existingMember)
                    }
                    .orElseGet {
                        memberService.registerMember(
                            Member.registerSocial(
                                identity = MemberIdentity.social(
                                    email = command.email,
                                    socialType = command.socialType,
                                    socialId = command.socialId
                                ),
                                profile = MemberProfile.social(command.memberName)
                            )
                        )
                    }
            }

        return RegisteredMemberResult.from(member)
    }
}
