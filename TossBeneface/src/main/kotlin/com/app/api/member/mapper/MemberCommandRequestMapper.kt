package com.app.api.member.mapper

import com.app.api.member.dto.ChangeMemberAuthorityDto
import com.app.api.member.dto.UpdateMemberInitialStateDto
import com.app.api.member.dto.UpdateMemberProfileDto
import com.app.domain.member.constant.Role
import com.app.member.application.dto.ChangeMemberAuthorityCommand
import com.app.member.application.dto.UpdateMemberInitialStateCommand
import com.app.member.application.dto.UpdateMemberProfileCommand
import org.springframework.stereotype.Component

@Component
class MemberCommandRequestMapper {

    fun toCommand(memberId: Long, request: UpdateMemberProfileDto.Request): UpdateMemberProfileCommand {
        return UpdateMemberProfileCommand(
            memberId = memberId,
            memberName = request.memberName,
            phoneNumber = request.phoneNumber,
            gender = request.gender,
            profileImg = request.profileImg
        )
    }

    fun toCommand(memberId: Long, request: UpdateMemberInitialStateDto.Request): UpdateMemberInitialStateCommand {
        return UpdateMemberInitialStateCommand(
            memberId = memberId,
            initialBudget = request.initialBudget,
            memberStatus = request.memberStatus
        )
    }

    fun toCommand(memberId: Long, requesterRole: Role, request: ChangeMemberAuthorityDto.Request): ChangeMemberAuthorityCommand {
        return ChangeMemberAuthorityCommand(
            memberId = memberId,
            role = request.role,
            requesterRole = requesterRole
        )
    }
}
