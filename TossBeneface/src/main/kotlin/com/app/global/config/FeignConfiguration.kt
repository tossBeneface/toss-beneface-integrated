package com.app.global.config

import com.app.global.error.FeignClientExceptionErrorDecoder
import feign.Logger
import feign.Retryer
import feign.codec.ErrorDecoder
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.cloud.openfeign.FeignAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableFeignClients(basePackages = ["com.app.api"])
@ImportAutoConfiguration(FeignAutoConfiguration::class, HttpClientConfiguration::class)
class FeignConfiguration {

    @Bean
    fun feignLoggerLevel(): Logger.Level {
        return Logger.Level.FULL
    }

    @Bean
    fun errorDecoder(): ErrorDecoder {
        return FeignClientExceptionErrorDecoder()
    }

    @Bean
    fun retryer(): Retryer {
        return Retryer.Default(1000, 2000, 3)
    }
}
