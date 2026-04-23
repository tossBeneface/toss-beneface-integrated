package com.app.api.cardbenefit.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class CardBenefitResponse(
    @JsonProperty("Benefit")
    val benefit: Int,
    @JsonProperty("limit_once")
    val limitOnce: Int,
    @JsonProperty("limit_month")
    val limitMonth: Int,
    @JsonProperty("min_pay")
    val minPay: Int,
    @JsonProperty("min_per")
    val minPer: Int,
    val monthly: Int,
    @JsonProperty("card_image")
    val cardImage: String?
)
