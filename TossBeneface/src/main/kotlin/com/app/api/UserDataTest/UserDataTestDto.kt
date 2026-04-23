package com.app.api.UserDataTest

data class UserDataTestDto(
    var memberId: Long? = null,
    var cardNumber: String? = null,
    var cvc: Int = 0,
    var pwd: Int = 0,
    var expiryDate: String? = null
)
