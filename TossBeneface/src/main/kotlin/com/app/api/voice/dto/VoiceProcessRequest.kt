package com.app.api.voice.dto

data class VoiceProcessRequest(
    val text: String,
    val brand: String,
    val memberId: Long? = null,
    val audioUrl: String? = null
)
