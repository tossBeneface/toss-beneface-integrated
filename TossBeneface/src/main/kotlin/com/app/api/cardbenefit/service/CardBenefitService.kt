package com.app.api.cardbenefit.service

import com.app.api.cardbenefit.dto.CardBenefitResponse
import com.app.domain.cardbenefit.entity.CardBenefit
import com.app.domain.cardbenefit.repository.CardBenefitRepository
import com.app.global.config.CacheConfig
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CardBenefitService(
    private val cardBenefitRepository: CardBenefitRepository
) {

    @Transactional
    fun saveCardBenefit(cardBenefit: CardBenefit): String {
        cardBenefitRepository.save(cardBenefit)
        return "카드 혜택 데이터가 성공적으로 저장되었습니다!"
    }

    @Cacheable(value = [CacheConfig.CARD_BENEFITS_BY_SHOP_CACHE], key = "#shop")
    fun getCardBenefitsByShop(shop: String): List<CardBenefit> {
        return cardBenefitRepository.findByShop(shop)
    }

    @Cacheable(value = [CacheConfig.CARD_BENEFIT_DETAILS_CACHE], key = "#carCompany + ':' + #cardName")
    fun getCardBenefitDetails(carCompany: String, cardName: String): List<CardBenefitResponse> {
        val benefitList = cardBenefitRepository.findByCardNameAndCardCompanyFetchCard(cardName, carCompany)
        
        return benefitList.map { entity ->
            CardBenefitResponse(
                benefit = entity.benefit,
                limitOnce = entity.limitOnce,
                limitMonth = entity.limitMonth,
                minPay = entity.minPay,
                minPer = entity.minPer,
                monthly = entity.monthly,
                cardImage = entity.card.cardImage
            )
        }
    }
}
