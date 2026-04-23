package com.app.api.cardbenefit.service

import com.app.domain.cardbenefit.entity.CardBenefit
import com.app.domain.cardbenefit.repository.CardBenefitRepository
import com.app.global.config.CacheConfig
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.cache.concurrent.ConcurrentMapCacheManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(classes = [CardBenefitCachingTest.Config::class, CardBenefitService::class])
@ActiveProfiles("test")
class CardBenefitCachingTest {

    @Autowired
    private lateinit var cardBenefitService: CardBenefitService

    @Autowired
    private lateinit var cardBenefitRepository: CardBenefitRepository

    @TestConfiguration
    @EnableCaching
    class Config {
        @Bean
        fun cardBenefitRepository(): CardBenefitRepository = mockk()

        @Bean
        fun cacheManager(): CacheManager {
            return ConcurrentMapCacheManager(
                CacheConfig.CARD_BENEFITS_BY_SHOP_CACHE,
                CacheConfig.CARD_BENEFIT_DETAILS_CACHE
            )
        }
    }

    @Test
    @DisplayName("getCardBenefitsByShop 호출 시 결과가 캐싱되어 두 번째 호출부터는 레포지토리를 접근하지 않는다")
    fun getCardBenefitsByShopCaching() {
        // given
        val shop = "스타벅스"
        val cardBenefit = mockk<CardBenefit>()
        every { cardBenefitRepository.findByShop(shop) } returns listOf(cardBenefit)

        // when
        cardBenefitService.getCardBenefitsByShop(shop) // 첫 번째 호출
        cardBenefitService.getCardBenefitsByShop(shop) // 두 번째 호출 (캐시 사용)

        // then
        verify(exactly = 1) { cardBenefitRepository.findByShop(shop) }
    }
}
