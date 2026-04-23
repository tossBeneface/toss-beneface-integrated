package com.app.api.payment

enum class PayType(val description: String) {
    CARD("카드"),
    CASH("현금"),
    POINT("포인트");
}
