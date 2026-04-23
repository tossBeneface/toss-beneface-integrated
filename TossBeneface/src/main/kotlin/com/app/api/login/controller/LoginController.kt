package com.app.api.login.controller

import com.app.api.login.dto.JoinDto
import com.app.api.login.dto.LoginDto
import com.app.auth.application.usecase.JoinUseCase
import com.app.auth.application.usecase.LoginUseCase
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@Tag(name = "authentication", description = "로그인/로그아웃/토큰재발급 API")
@RestController
@CrossOrigin(origins = ["*"], allowedHeaders = ["*"], methods = [RequestMethod.GET, RequestMethod.POST, RequestMethod.OPTIONS])
@RequestMapping("/api")
class LoginController(
    private val joinUseCase: JoinUseCase,
    private val loginUseCase: LoginUseCase
) {
    private val log = LoggerFactory.getLogger(LoginController::class.java)

    @Tag(name = "authentication")
    @Operation(summary = "회원가입 API", description = "회원가입 API")
    @PostMapping("/join")
    fun join(@RequestBody joinRequestDto: JoinDto.Request, httpServletResponse: HttpServletResponse): ResponseEntity<JoinDto.Response> {
        log.info("회원가입 요청 수신: {}", joinRequestDto)
        val joinResponseDto = joinUseCase.join(joinRequestDto, httpServletResponse)
        return ResponseEntity.ok(joinResponseDto)
    }

    @Tag(name = "authentication")
    @Operation(summary = "로그인 API", description = "로그인 API")
    @PostMapping("/login")
    fun login(@RequestBody loginRequestDto: LoginDto.Request, response: HttpServletResponse): ResponseEntity<LoginDto.Response> {
        val jwtTokenResponseDto = loginUseCase.login(loginRequestDto, response)
        return ResponseEntity.ok(jwtTokenResponseDto)
    }
}
