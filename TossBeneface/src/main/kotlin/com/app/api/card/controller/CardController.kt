package com.app.api.card.controller

import com.app.api.card.service.CardService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/cards")
class CardController(
    private val cardService: CardService
) {

    @GetMapping("/image")
    fun getCardImage(
        @RequestParam("card_name") cardName: String,
        @RequestParam("card_company") cardCompany: String
    ): ResponseEntity<String> {
        val cardImage = cardService.getCardImage(cardName, cardCompany)
        return ResponseEntity.ok(cardImage)
    }
}
