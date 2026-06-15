package com.app.member.application.usecase

import com.app.domain.member.entity.Member
import com.app.domain.member.service.MemberService
import com.app.member.application.dto.MemberInfoResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class GetMemberInfoUseCase(
    private val memberService: MemberService
) {

    fun getMemberInfo(memberId: Long): MemberInfoResult {
        return memberService.findMemberById(memberId).toResult()
    }

    fun getMemberName(memberId: Long): String {
        return memberService.findMemberById(memberId).memberName
    }

    private fun Member.toResult(): MemberInfoResult {
        return MemberInfoResult(
            memberId = memberId.toString(),
            memberName = memberName,
            email = email,
            phoneNumber = phoneNumber,
            gender = gender.toString(),
            budget = budget ?: 0,
            profileImg = profileImg,
            role = role.toString()
        )
    }
}
