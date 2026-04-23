package com.app.global.kafka.event

import java.time.LocalDateTime

data class VoiceRequestedEvent(
    val requestId: String,      // UUID — 요청 추적용
    val memberId: Long,         // Partition Key — 한 사용자의 요청은 순서 보장
    val audioUrl: String? = null,       // S3에 업로드된 음성 파일 URL
    val text: String? = null,
    val brand: String? = null,
    val menus: List<String> = emptyList(),
    val requestedAt: LocalDateTime = LocalDateTime.now()
)

data class VoiceCompletedEvent(
    val requestId: String,      // VoiceRequestedEvent와 매핑
    val memberId: Long,
    val result: String,         // AI 처리 결과 텍스트
    val completedAt: LocalDateTime = LocalDateTime.now()
)
