package com.app.api.member.dto

import com.app.domain.member.constant.OnboardingStatus
import com.app.domain.member.constant.OnboardingStep
import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDateTime

data class MemberOnboardingResponseDto(
    @field:Schema(description = "MemberId", example = "1", required = true)
    val memberId: Long,

    @field:Schema(description = "온보딩 진행 상태", example = "IN_PROGRESS", required = true)
    val onboardingStatus: OnboardingStatus,

    @field:Schema(description = "다음 진행할 온보딩 단계", example = "BUDGET", required = true)
    val onboardingStep: OnboardingStep,

    @field:Schema(description = "온보딩 완료 시각", example = "2024-03-23 23:18:14", required = false)
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val onboardingCompletedAt: LocalDateTime? = null
)
