package com.app.api.token.dto

import com.fasterxml.jackson.annotation.JsonFormat
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

data class AccessTokenResponseDto(
    @field:Schema(description = "grantType", example = "Bearer", required = true)
    val grantType: String,

    @field:Schema(description = "accessToken", example = "...", required = true)
    val accessToken: String,

    @field:Schema(description = "access token 만료 시간", example = "2024-03-23 23:18:14", required = true)
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "Asia/Seoul")
    val accessTokenExpireTime: Date
)
