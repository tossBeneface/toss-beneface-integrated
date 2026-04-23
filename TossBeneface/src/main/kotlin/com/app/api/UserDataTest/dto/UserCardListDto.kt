package com.app.api.UserDataTest.dto

data class UserCardListDto(
    val cardId: Long,
    val cardName: String,
    val cardCompany: String,
    val cardImage: String,
    val accrueBenefit: Int,
    val cardNumber: String,
    val expiryDate: String,
    val cvc: Int
)
