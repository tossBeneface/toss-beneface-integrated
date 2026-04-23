package com.app.api.card.dto

import org.springframework.web.multipart.MultipartFile

data class OcrCardRequestDto(
    var image: MultipartFile? = null
)
