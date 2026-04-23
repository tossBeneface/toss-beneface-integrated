package com.app.domain.cardbenefit.repository

import com.app.domain.cardbenefit.entity.CardBenefit
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface CardBenefitRepository : JpaRepository<CardBenefit, Long> {

    fun findByShop(shop: String): List<CardBenefit>

    fun findByCardNameAndCardCompany(cardName: String, corp: String): List<CardBenefit>

    @Query("SELECT cb FROM CardBenefit cb JOIN FETCH cb.card c " +
            "WHERE cb.cardName = :cardName AND cb.cardCompany = :corp")
    fun findByCardNameAndCardCompanyFetchCard(
        @Param("cardName") cardName: String,
        @Param("corp") corp: String
    ): List<CardBenefit>
}
