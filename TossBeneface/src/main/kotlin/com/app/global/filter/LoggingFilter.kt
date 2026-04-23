package com.app.global.filter

import jakarta.servlet.*
import jakarta.servlet.http.HttpServletRequest
import org.springframework.stereotype.Component
import java.io.IOException

@Component
class LoggingFilter : Filter {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val req = request as HttpServletRequest
        println("Request URL: ${req.requestURI}")
        chain.doFilter(request, response)
    }
}
