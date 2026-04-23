package com.app.api.cardbenefit.service

import com.app.domain.cardbenefit.entity.CardBenefit
import com.app.domain.cardbenefit.repository.CardBenefitRepository
import com.app.domain.card.entity.Card
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test

class CardBenefitServiceTest {

    private val cardBenefitRepository = mockk<CardBenefitRepository>()
    private val cardBenefitService = CardBenefitService(cardBenefitRepository)

    @Test
    @DisplayName("상점 이름으로 카드 혜택 목록을 조회한다")
    fun getCardBenefitsByShop() {
        // given
        val shop = "스타벅스"
        val cardBenefit = mockk<CardBenefit>()
        every { cardBenefitRepository.findByShop(shop) } returns listOf(cardBenefit)

        // when
        val result = cardBenefitService.getCardBenefitsByShop(shop)

        // then
        assertEquals(1, result.size)
        verify { cardBenefitRepository.findByShop(shop) }
    }

    @Test
    @DisplayName("카드사와 카드 이름으로 상세 혜택을 조회한다")
    fun getCardBenefitDetails() {
        // given
        val cardCompany = "신한카드"
        val cardName = "Deep Dream"
        
        val card = mockk<Card>()
        every { card.cardImage } returns "image_url"
        
        val cardBenefit = mockk<CardBenefit>()
        every { cardBenefit.benefit } returns 10
        every { cardBenefit.limitOnce } returns 1000
        every { cardBenefit.limitMonth } returns 5000
        every { cardBenefit.minPay } returns 0
        every { cardBenefit.minPer } returns 0
        every { cardBenefit.monthly } returns 1
        every { cardBenefit.card } returns card
        
        every { cardBenefitRepository.findByCardNameAndCardCompanyFetchCard(cardName, cardCompany) } returns listOf(cardBenefit)

        // when
        val result = cardBenefitService.getCardBenefitDetails(cardCompany, cardName)

        // then
        assertEquals(1, result.size)
        assertEquals("10% 할인", result[0].benefit)
        assertEquals("image_url", result[0].cardImage)
        verify { cardBenefitRepository.findByCardNameAndCardCompanyFetchCard(cardName, cardCompany) }
    }
}
