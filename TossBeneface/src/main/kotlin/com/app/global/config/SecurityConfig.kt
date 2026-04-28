package com.app.global.config

import com.app.auth.infra.oauth2.CustomOAuth2UserService
import com.app.auth.infra.oauth2.OAuth2SuccessHandler
import com.app.global.filter.JwtAuthenticationFilter
import com.app.global.filter.LoggingFilter
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.ObjectProvider
import org.springframework.beans.factory.annotation.Value
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
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val clientRegistrationRepositoryProvider: ObjectProvider<ClientRegistrationRepository>,
    @Value("\${app.oauth2.redirect-uri:http://localhost:3000}") private val redirectUri: String
) {

    private val log = LoggerFactory.getLogger(SecurityConfig::class.java)

    @Bean
    fun bCryptPasswordEncoder(): BCryptPasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        log.info("SecurityConfig - SecurityFilterChain 설정 시작 (STATELESS)")
        http
            .cors { cors -> cors.configurationSource(corsConfigurationSource()) }
            .csrf { csrf -> csrf.disable() }
            .sessionManagement { session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info", "/actuator/prometheus").permitAll()
                    .requestMatchers(
                        // 인증 불필요: 공개 API
                        "/swagger-ui/**", "/v3/api-docs/**",
                        "/api/health", "/api/join", "/api/login", "/api/access-token/issue", "/h2-console/**",
                        "/api/qnaboard/**", "/api/member/info", "/api/card-benefits", "/api/flow",
                        "/api/card-benefits/**", "/api/products/**", "/api/member/name/**",
                        "/api/user-data-test/**", "/api/faces/**",
                        "/api/qr/generate", "/api/qr/authenticate",
                        // Toss 결제 리다이렉트 콜백
                        "/api/payment/", "/api/payment/fail", "/api/payment/callback-auth",
                        // WebSocket / OAuth2
                        "/ws/**", "/oauth2/**", "/login/oauth2/**"
                    ).permitAll()
                    // 주문·결제·사용자 카드는 인증 필수 (JwtAuthenticationFilter에서 채워진 SecurityContext 사용)
                    .requestMatchers("/api/orders/**", "/api/payment/**", "/api/user-cards/**").authenticated()
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
            .addFilterBefore(LoggingFilter(), UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
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
                oauth2.failureHandler { _, response, ex ->
                    log.warn("OAuth2 login failed: {}", ex.message)
                    response.sendRedirect("$redirectUri?error=oauth2_failed")
                }
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
