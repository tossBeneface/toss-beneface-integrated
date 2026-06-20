package com.app.api.UserDataTest

import com.app.domain.cardbenefit.entity.CardBenefit
import com.app.api.UserDataTest.dto.UserCardListDto
import com.app.domain.card.entity.Card
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserDataTestRepository : JpaRepository<UserDataTestEntity, Long> {

    @Query("SELECT DISTINCT u.cardName, u.cardCompany FROM UserDataTestEntity u WHERE u.member.memberId = :memberId")
    fun findCardDetailsByMemberId(@Param("memberId") memberId: Long): List<Array<Any>>

    @Query("""
        SELECT cb FROM CardBenefit cb 
        JOIN UserDataTestEntity u ON cb.cardName = u.cardName AND cb.cardCompany = u.cardCompany
        WHERE u.member.memberId = :memberId
    """)
    fun findCardBenefitsByMemberId(@Param("memberId") memberId: Long): List<CardBenefit>

    @Query("""
        SELECT u.cardName, u.cardCompany, u.lastPer, u.payAmount, u.monthlySplit, u.nowPer, u.accrueBenefit 
        FROM UserDataTestEntity u WHERE u.member.memberId = :memberId
    """)
    fun findFinancialDataByMemberId(@Param("memberId") memberId: Long): List<Array<Any>>

    @Query("""
        SELECT new com.app.api.UserDataTest.dto.UserCardListDto(
            u.id, c.cardName, c.cardCompany, c.cardImage, 
            COALESCE(u.accrueBenefit, 0), u.cardNumber, u.expiryDate, u.cvc
        ) 
        FROM UserDataTestEntity u 
        JOIN u.card c 
        WHERE u.member.memberId = :memberId
    """)
    fun findCardListByMemberId(@Param("memberId") memberId: Long): List<UserCardListDto>

    @Query("SELECT c FROM Card c WHERE c.cardName = :cardName AND c.cardCompany = :cardCompany")
    fun findCardByNameAndCompany(@Param("cardName") cardName: String, @Param("cardCompany") cardCompany: String): Optional<Card>

    @Query("SELECT u.member.memberId FROM UserDataTestEntity u WHERE u.id = :cardId")
    fun findOwnerMemberIdById(@Param("cardId") cardId: Long): Long?
}
