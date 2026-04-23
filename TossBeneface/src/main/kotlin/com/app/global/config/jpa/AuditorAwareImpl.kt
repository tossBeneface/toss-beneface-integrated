package com.app.global.config.jpa

import jakarta.servlet.http.HttpServletRequest
import org.springframework.data.domain.AuditorAware
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.util.*

class AuditorAwareImpl : AuditorAware<String> {

    override fun getCurrentAuditor(): Optional<String> {
        val attributes = RequestContextHolder.getRequestAttributes()
        if (attributes is ServletRequestAttributes) {
            val request: HttpServletRequest = attributes.request
            val modifiedBy = request.requestURI

            return if (modifiedBy.isNullOrBlank()) {
                Optional.of("unknown")
            } else {
                Optional.of(modifiedBy)
            }
        }
        return Optional.of("unknown")
    }
}
