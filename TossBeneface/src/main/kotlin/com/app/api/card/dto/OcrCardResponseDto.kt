package com.app.api.card.dto

data class OcrCardResponseDto(
    val cardDetected: Boolean,
    val cardNumber: String?,
    val cardName: String?,
    val cardCompany: String?,
    val cardImage: String?,
    val dateInfo: String?,
    val cvcInfo: String?,
    val otherText: String?
)
