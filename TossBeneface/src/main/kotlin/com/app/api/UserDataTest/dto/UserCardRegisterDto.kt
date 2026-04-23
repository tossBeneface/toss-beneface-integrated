package com.app.api.UserDataTest.dto

data class UserCardRegisterDto(
    var cardName: String? = null,
    var cardCompany: String? = null,
    var cardNumber: String? = null,
    var expiryDate: String? = null,
    var cvc: Int = 0,
    var pwd: Int = 0
)
