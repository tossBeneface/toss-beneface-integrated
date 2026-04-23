package com.app.api.card.service

import com.app.api.card.repository.CardRepository
import com.app.global.error.ErrorCode
import com.app.global.error.exception.EntityNotFoundException
import org.springframework.stereotype.Service

@Service
class CardService(
    private val cardRepository: CardRepository
) {
    fun getCardImage(cardName: String, cardCompany: String): String {
        return cardRepository.findByCardNameAndCardCompany(cardName, cardCompany)
            .map { it.cardImage }
            .orElseThrow { EntityNotFoundException(ErrorCode.CARD_NOT_FOUND) } ?: throw EntityNotFoundException(ErrorCode.CARD_NOT_FOUND)
    }
}
