package com.app.global.config

import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.HttpClients
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class HttpClientConfiguration {

    @Bean
    fun httpClient(): CloseableHttpClient {
        return HttpClients.createDefault()
    }
}
