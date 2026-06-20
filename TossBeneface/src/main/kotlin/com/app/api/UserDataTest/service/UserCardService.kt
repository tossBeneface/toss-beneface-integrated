package com.app.api.UserDataTest.service

import com.app.api.UserDataTest.UserDataTestEntity
import com.app.api.UserDataTest.UserDataTestRepository
import com.app.api.UserDataTest.dto.UserCardListDto
import com.app.api.UserDataTest.dto.UserCardRegisterDto
import com.app.domain.member.repository.MemberRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
class UserCardService(
    private val repository: UserDataTestRepository,
    private val memberRepository: MemberRepository
) {

    fun getUserCards(memberId: Long): List<UserCardListDto> {
        return repository.findCardListByMemberId(memberId)
    }

    @Transactional
    fun registerCard(dto: UserCardRegisterDto, memberId: Long): String {
        val card = repository.findCardByNameAndCompany(dto.cardName!!, dto.cardCompany!!)
            .orElseThrow { IllegalArgumentException("존재하지 않는 카드입니다.") }

        val member = memberRepository.findById(memberId)
            .orElseThrow { IllegalArgumentException("존재하지 않는 회원입니다. memberId: $memberId") }

        val random = Random()
        val nowPer = random.nextInt(90001) * 10
        val lastPer = (random.nextInt((100000 - (nowPer / 10)) + 1) + (nowPer / 10)) * 10
        val cardLimit = (random.nextInt((1000000 - 100000) + 1) + 100000) * 10
        val monthlySplit = random.nextInt(4)
        val accrueBenefit = (random.nextInt((5000 - 100) + 1) + 100) * 10

        val userDataTest = UserDataTestEntity(
            member = member,
            card = card,
            cardName = dto.cardName!!,
            cardCompany = dto.cardCompany!!,
            cardNumber = dto.cardNumber!!,
            expiryDate = dto.expiryDate!!,
            cvc = dto.cvc,
            pwd = dto.pwd,
            nowPer = nowPer,
            lastPer = lastPer,
            cardLimit = cardLimit,
            monthlySplit = monthlySplit,
            accrueBenefit = accrueBenefit
        )

        repository.save(userDataTest)
        return "카드 정보 저장 성공"
    }

    @Transactional
    fun deleteUserCard(cardId: Long) {
        if (!repository.existsById(cardId)) {
            throw IllegalArgumentException("삭제할 카드가 없습니다.")
        }
        repository.deleteById(cardId)
    }
}
