package com.app.api.card.repository

import com.app.domain.card.entity.CardBin
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface CardBinRepository : JpaRepository<CardBin, Long> {
    @Query("SELECT cb FROM CardBin cb WHERE cb.cardBin = :cardNumber")
    fun findByCardNumber(@Param("cardNumber") cardNumber: String): Optional<CardBin>
}
