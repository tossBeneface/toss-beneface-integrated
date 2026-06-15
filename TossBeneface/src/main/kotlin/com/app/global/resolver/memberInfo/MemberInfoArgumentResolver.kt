package com.app.global.resolver.memberInfo

import com.app.auth.infra.security.AuthenticatedMemberContextResolver
import jakarta.servlet.http.HttpServletRequest
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer

@Component
class MemberInfoArgumentResolver(
    private val authenticatedMemberContextResolver: AuthenticatedMemberContextResolver
) : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean {
        val hasMemberInfoAnnotation = parameter.hasParameterAnnotation(MemberInfo::class.java)
        val hasMemberInfoDto = MemberInfoDto::class.java.isAssignableFrom(parameter.parameterType)
        return hasMemberInfoAnnotation && hasMemberInfoDto
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?
    ): Any? {
        val request = webRequest.getNativeRequest(HttpServletRequest::class.java)
            ?: throw IllegalStateException("HttpServletRequest not found")
        val authenticatedMember = authenticatedMemberContextResolver.resolve(request)

        return MemberInfoDto(
            memberId = authenticatedMember.memberId,
            role = authenticatedMember.role
        )
    }
}
