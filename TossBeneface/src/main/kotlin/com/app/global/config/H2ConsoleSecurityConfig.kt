package com.app.global.config

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.security.servlet.PathRequest.toH2Console
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer

@Configuration
@ConditionalOnProperty(name = ["spring.h2.console.enabled"], havingValue = "true")
class H2ConsoleSecurityConfig {

    @Bean
    fun h2ConsoleCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web -> web.ignoring().requestMatchers(toH2Console()) }
    }
}
