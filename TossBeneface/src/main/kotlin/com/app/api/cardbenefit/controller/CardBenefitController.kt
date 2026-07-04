package com.app.api.cardbenefit.controller

import com.app.api.cardbenefit.service.CardBenefitService
import com.app.domain.cardbenefit.entity.CardBenefit
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/card-benefits")
class CardBenefitController(
    private val cardBenefitService: CardBenefitService
) {

    @PreAuthorize("@authz.can('benefit', 'manage')")
    @PostMapping
    fun saveCardBenefit(@RequestBody cardBenefit: CardBenefit): String {
        return cardBenefitService.saveCardBenefit(cardBenefit)
    }

    @GetMapping("/get_shop")
    fun getCardBenefitsByShop(@RequestParam shop: String): List<CardBenefit> {
        return cardBenefitService.getCardBenefitsByShop(shop)
    }

    @GetMapping("/get_details")
    fun getCardBenefitDetails(
        @RequestParam("carcompany") carCompany: String,
        @RequestParam("card_name") cardName: String
    ): ResponseEntity<*> {
        val details = cardBenefitService.getCardBenefitDetails(carCompany, cardName)

        return if (details.isNotEmpty()) {
            ResponseEntity.ok(details)
        } else {
            ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body("No card benefit found for given parameters")
        }
    }
}
