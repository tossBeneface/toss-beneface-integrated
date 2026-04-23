package com.app.api.health.dto

import io.swagger.v3.oas.annotations.media.Schema

data class HealthCheckResponseDto(
    @field:Schema(description = "서버 health 상태", example = "ok", required = true)
    val health: String,

    @field:Schema(description = "현재 실행 중인 profile", example = "[dev]", required = true)
    val activeProfiles: List<String>
)
