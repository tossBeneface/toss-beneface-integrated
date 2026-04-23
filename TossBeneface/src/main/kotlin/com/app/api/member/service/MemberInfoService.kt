package com.app.api.member.service

import com.app.api.member.dto.MemberInfoResponseDto
import com.app.domain.member.service.MemberService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberInfoService(
    private val memberService: MemberService
) {

    @Transactional(readOnly = true)
    fun getMemberInfo(memberId: Long): MemberInfoResponseDto {
        val member = memberService.findMemberById(memberId)
        return MemberInfoResponseDto.of(member)
    }

    fun getMemberName(memberId: Long): String {
        val member = memberService.findMemberById(memberId)
        return member.memberName
    }
}
