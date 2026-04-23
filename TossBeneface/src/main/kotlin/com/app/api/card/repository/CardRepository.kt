package com.app.api.card.repository

import com.app.domain.card.entity.Card
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface CardRepository : JpaRepository<Card, Long> {
    fun findByCardNameAndCardCompany(cardName: String, cardCompany: String): Optional<Card>
}
