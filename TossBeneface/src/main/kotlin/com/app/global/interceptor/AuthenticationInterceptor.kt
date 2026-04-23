package com.app.global.interceptor

import com.app.auth.infra.security.JwtTokenProvider
import com.app.auth.infra.web.BearerTokenResolver
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class AuthenticationInterceptor(
    private val bearerTokenResolver: BearerTokenResolver,
    private val jwtTokenProvider: JwtTokenProvider
) : HandlerInterceptor {

    private val log = LoggerFactory.getLogger(AuthenticationInterceptor::class.java)

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        log.info("AuthenticationInterceptor 호출: {}", request.requestURI)

        val token = bearerTokenResolver.resolve(request)
        log.info("Authorization token resolved for {}", request.requestURI)
        val tokenClaims = jwtTokenProvider.parseAccessToken(token)
        log.debug("Received JWT Token: {}", token)
        log.debug("Parsed Claims: {}", tokenClaims)
        log.info("토큰 유효성 검사 완료")
        return true
    }
}
