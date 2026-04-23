package com.app.domain.member.service

import com.app.domain.member.entity.Member
import com.app.domain.member.repository.MemberRepository
import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import com.app.global.error.exception.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional
class MemberService(
    private val memberRepository: MemberRepository
) {

    fun registerMember(member: Member): Member {
        validateDuplicateMember(member)
        return memberRepository.save(member)
    }

    @Transactional(readOnly = true)
    fun findMemberByEmail(email: String): Optional<Member> {
        return memberRepository.findByEmail(email)
    }

    private fun validateDuplicateMember(member: Member) {
        val optionalMember = memberRepository.findByEmail(member.email)
        if (optionalMember.isPresent) {
            throw BusinessException(ErrorCode.ALREADY_REGISTERED_MEMBER)
        }
    }

    fun findMemberById(memberId: Long): Member {
        return memberRepository.findById(memberId)
            .orElseThrow { EntityNotFoundException(ErrorCode.MEMBER_NOT_EXIST) }
    }

    fun getMemberByEmail(email: String): Member? {
        return memberRepository.findByEmail(email).orElse(null)
    }

    fun updateMember(member: Member) {
        memberRepository.save(member)
    }
}
