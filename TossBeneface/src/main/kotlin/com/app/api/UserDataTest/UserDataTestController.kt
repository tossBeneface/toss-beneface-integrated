package com.app.api.UserDataTest

import com.app.domain.cardbenefit.entity.CardBenefit
import com.app.domain.cardbenefit.repository.CardBenefitRepository
import com.app.domain.member.repository.MemberRepository
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/user-data-test")
class UserDataTestController(
    private val userDataTestRepository: UserDataTestRepository,
    private val memberRepository: MemberRepository,
    private val cardBenefitRepository: CardBenefitRepository
) {

    @PostMapping
    fun saveUserData(@RequestBody userData: UserDataTestEntity): String {
        val memberId = userData.member.memberId ?: return "❌ Member ID is required!"
        val member = memberRepository.findById(memberId).orElse(null) ?: return "❌ Member not found!"

        if (userData.cardCompany == null) {
            return "❌ CardCompany and CardName are required!"
        }
        
        val cardBenefitList = cardBenefitRepository.findByCardNameAndCardCompany(
            userData.cardName, userData.cardCompany
        )
        if (cardBenefitList.isEmpty()) {
            return "❌ Card Benefit not found!"
        }
        
        userData.member = member
        userDataTestRepository.save(userData)
        return "✅ UserDataTestEntity saved successfully!"
    }

    @GetMapping("/cards")
    fun getCardsByMemberId(@RequestParam memberId: Long): List<String> {
        val cardDetails = userDataTestRepository.findCardDetailsByMemberId(memberId)
        return cardDetails.map { "${it[0]} - ${it[1]}" }
    }

    @GetMapping("/card-benefits")
    fun getCardBenefitsByMemberId(@RequestParam memberId: Long): List<CardBenefit> {
        return userDataTestRepository.findCardBenefitsByMemberId(memberId)
    }

    @GetMapping("/financial-data")
    fun getFinancialDataByMemberId(@RequestParam memberId: Long): List<Map<String, Any?>> {
        val results = userDataTestRepository.findFinancialDataByMemberId(memberId)
        return results.map { row ->
            mapOf(
                "card_name" to row[0],
                "card_company" to row[1],
                "last_per" to row[2],
                "pay_amount" to row[3],
                "monthly_split" to row[4],
                "now_per" to row[5],
                "Accrue_benefit" to row[6]
            )
        }
    }
}
