package com.app.api.token.controller

import com.app.api.token.dto.AccessTokenResponseDto
import com.app.auth.application.usecase.IssueAccessTokenUseCase
import com.app.auth.infra.web.RefreshTokenCookieManager
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
class TokenController(
    private val issueAccessTokenUseCase: IssueAccessTokenUseCase,
    private val refreshTokenCookieManager: RefreshTokenCookieManager
) {

    @Tag(name = "authentication")
    @Operation(summary = "Access Token 재발급 API", description = "Access Token 재발급 API")
    @PostMapping("/access-token/issue")
    fun createAccessToken(httpServletRequest: HttpServletRequest, response: HttpServletResponse): ResponseEntity<AccessTokenResponseDto> {
        val refreshToken = refreshTokenCookieManager.extractRefreshToken(httpServletRequest)

        val tokenResponse = issueAccessTokenUseCase.issue(refreshToken, response)

        return ResponseEntity.ok(
            AccessTokenResponseDto(
                grantType = tokenResponse.grantType!!,
                accessToken = tokenResponse.accessToken!!,
                accessTokenExpireTime = tokenResponse.accessTokenExpireTime!!
            )
        )
    }
}
