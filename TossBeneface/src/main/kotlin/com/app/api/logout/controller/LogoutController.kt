package com.app.api.logout.controller

import com.app.auth.application.usecase.LogoutUseCase
import com.app.auth.infra.web.BearerTokenResolver
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "authentication", description = "로그인/로그아웃/토큰재발급 API")
@RestController
@RequestMapping("/api")
class LogoutController(
    private val bearerTokenResolver: BearerTokenResolver,
    private val logoutUseCase: LogoutUseCase
) {

    @Tag(name = "authentication")
    @Operation(summary = "로그아웃 API", description = "로그아웃 시 refresh token 만료 처리")
    @PostMapping("/logout")
    fun logout(httpServletRequest: HttpServletRequest, response: HttpServletResponse): ResponseEntity<String> {
        val accessToken = bearerTokenResolver.resolve(httpServletRequest)
        logoutUseCase.logout(accessToken, response)
        return ResponseEntity.noContent().build()
    }
}
