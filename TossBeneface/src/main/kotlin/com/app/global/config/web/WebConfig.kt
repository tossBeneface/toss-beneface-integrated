package com.app.global.config.web

import com.app.global.interceptor.AdminAuthorizationInterceptor
import com.app.global.interceptor.AuthenticationInterceptor
import com.app.global.resolver.memberInfo.MemberInfoArgumentResolver
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Configuration
class WebConfig(
    private val authenticationInterceptor: AuthenticationInterceptor,
    private val adminAuthorizationInterceptor: AdminAuthorizationInterceptor,
    private val memberInfoArgumentResolver: MemberInfoArgumentResolver,
    private val objectMapper: ObjectMapper
) : WebMvcConfigurer {

    private val log = LoggerFactory.getLogger(WebConfig::class.java)

    override fun addInterceptors(registry: InterceptorRegistry) {
        log.info("WebConfig - Interceptor 등록됨")
        registry.addInterceptor(authenticationInterceptor)
            .order(1)
            .addPathPatterns("/api/**")
            .excludePathPatterns(
                "/swagger-ui/**", "/v3/api-docs/**", "/h2-console/**",
                "/api/join", "/api/join/**", "/api/login", "/api/login/**",
                "/api/access-token/issue", "/api/access-token/issue/**", "/api/health", "/api/qnaboard/test",
                "/api/card-benefits", "/api/card-benefits/**", "/api/user-data-test", "/api/user-data-test/**",
                "/api/v1/payments/**", "/api/faces/**", "/api/products/**", "/api/flow", "/api/member/name/**",
                "/api/qr/authenticate", "/api/orders/**"
            )
        
        registry.addInterceptor(adminAuthorizationInterceptor)
            .order(2)
            .addPathPatterns("/api/admin/**")
        
        log.debug("등록된 인터셉터 경로: /api/**")
    }

    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(memberInfoArgumentResolver)
    }

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        // resource handlers implementation if needed
    }

    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}
