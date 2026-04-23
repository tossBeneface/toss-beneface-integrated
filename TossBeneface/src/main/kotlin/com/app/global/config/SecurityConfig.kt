package com.app.global.config

import com.app.auth.infra.oauth2.CustomOAuth2UserService
import com.app.auth.infra.oauth2.OAuth2SuccessHandler
import com.app.global.filter.LoggingFilter
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource

@Configuration
class SecurityConfig(
    private val customOAuth2UserService: CustomOAuth2UserService,
    private val oAuth2SuccessHandler: OAuth2SuccessHandler,
    private val clientRegistrationRepositoryProvider: ObjectProvider<ClientRegistrationRepository>
) {

    private val log = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        log.info("SecurityConfig - SecurityFilterChain 설정 시작")
        http
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .csrf { csrf -> csrf.disable() }
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/actuator/prometheus").permitAll()
                    .requestMatchers(
                        "/swagger-ui/**", "/v3/api-docs/**",
                        "/api/health", "/api/join", "/api/login", "/api/access-token/issue", "/h2-console/**",
                        "/api/qnaboard/**", "/api/member/info", "/api/card-benefits", "/api/flow", "/api/payments/**", "/payment/**", "/success/**", "http://localhost:8080/api/v1/payments/toss/fail/**", "http://localhost:8080/api/v1/payments/toss/success/**", "/api/payment/**", "/fail/**", "https://api.tosspayments.com/v1/payments/confirm/**",
                        "/api/user-data-test/**", "/api/faces/**", "/api/card-benefits/**", "/api/products/**", "/api/member/name/**", "/api/qr/generate", "/api/qr/authenticate", "/api/user-cards/**", "/api/orders/**",
                        "/ws/**", "/oauth2/**", "/login/oauth2/**"
                    ).permitAll()
                    .anyRequest().authenticated()
            }
            .headers { headers ->
                headers.frameOptions { it.disable() }
                    .contentSecurityPolicy { csp ->
                        csp.policyDirectives(
                            "default-src 'self'; " +
                                "script-src 'self' 'unsafe-inline'; " +
                                "style-src 'self' 'unsafe-inline'; " +
                                "img-src 'self' https://static.toss.im;"
                        )
                    }
            }
            .securityContext { it.requireExplicitSave(false) }
            .addFilterBefore(LoggingFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .exceptionHandling { exceptions ->
                exceptions
                    .authenticationEntryPoint(authenticationEntryPoint())
                    .accessDeniedHandler(accessDeniedHandler())
            }

        if (clientRegistrationRepositoryProvider.ifAvailable != null) {
            http.oauth2Login { oauth2 ->
                oauth2.userInfoEndpoint { userInfo ->
                    userInfo.userService(customOAuth2UserService)
                }
                oauth2.successHandler(oAuth2SuccessHandler)
            }
        }
        return http.build()
    }

    @Bean
    fun authenticationEntryPoint(): AuthenticationEntryPoint {
        log.info("AuthenticationEntryPoint 빈 생성")
        return AuthenticationEntryPoint { _, response, authException ->
            log.warn("401 Unauthorized Error: {}", authException.message)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
        }
    }

    @Bean
    fun accessDeniedHandler(): AccessDeniedHandler {
        log.info("AccessDeniedHandler 빈 생성")
        return AccessDeniedHandler { _, response, accessDeniedException ->
            log.warn("403 Forbidden Error: {}", accessDeniedException.message)
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden")
        }
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOriginPatterns = listOf(
            "http://localhost:3000",
            "http://127.0.0.1:3000",
            "https://app.tossbeneface.com",
            "https://www.tossbeneface.com",
            "https://d2c33voyig3fqp.cloudfront.net",
            "https://d1o4yxmvaw5lsd.cloudfront.net"
        )
        config.allowedMethods = listOf("HEAD", "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
        config.allowedHeaders = listOf("Authorization", "Content-Type", "Accept")
        config.allowCredentials = true
        config.exposedHeaders = listOf("Set-Cookie")

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}
