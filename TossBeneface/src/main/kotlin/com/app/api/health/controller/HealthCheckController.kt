package com.app.api.health.controller

import com.app.api.health.dto.HealthCheckResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.core.env.Environment
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "health check", description = "서버 상태 체크 API")
@RestController
@RequestMapping("/api")
class HealthCheckController(
    private val environment: Environment
) {

    @Tag(name = "health check")
    @Operation(summary = "서버 Health Check API", description = "현재 서버가 정상적으로 기동이 된 상태인지 검사하는 API")
    @GetMapping("/health")
    fun healthCheck(): ResponseEntity<HealthCheckResponseDto> {
        val healthCheckResponseDto = HealthCheckResponseDto(
            health = "ok",
            activeProfiles = environment.activeProfiles.toList()
        )
        return ResponseEntity.ok(healthCheckResponseDto)
    }
}
