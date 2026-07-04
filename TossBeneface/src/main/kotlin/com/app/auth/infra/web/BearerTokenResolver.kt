package com.app.auth.infra.web

import com.app.global.error.ErrorCode
import com.app.global.error.exception.AuthenticationException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes

@Component
class BearerTokenResolver {

    fun resolve(request: HttpServletRequest): String {
        val authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (StringUtils.hasText(authorizationHeader)) {
            return resolve(authorizationHeader)
        }

        val accessTokenCookie = request.cookies
            ?.firstOrNull { it.name == AuthCookieNames.ACCESS_TOKEN }
            ?.value

        if (StringUtils.hasText(accessTokenCookie)) {
            return accessTokenCookie!!
        }

        throw AuthenticationException(ErrorCode.NOT_EXISTS_AUTHORIZATION)
    }

    fun resolve(authorizationHeader: String?): String {
        if (!StringUtils.hasText(authorizationHeader)) {
            throw AuthenticationException(ErrorCode.NOT_EXISTS_AUTHORIZATION)
        }

        val authorizations = authorizationHeader!!.split(" ".toRegex(), limit = 2).toTypedArray()
        if (authorizations.size < 2 || BEARER_GRANT_TYPE != authorizations[0] || !StringUtils.hasText(authorizations[1])) {
            throw AuthenticationException(ErrorCode.NOT_VALID_BEARER_GRANT_TYPE)
        }

        return authorizations[1]
    }

    fun resolveFromCurrentRequest(): String {
        val attributes = RequestContextHolder.getRequestAttributes() as? ServletRequestAttributes
            ?: throw AuthenticationException(ErrorCode.NOT_EXISTS_AUTHORIZATION)

        return resolve(attributes.request)
    }

    companion object {
        private const val BEARER_GRANT_TYPE = "Bearer"
    }
}
