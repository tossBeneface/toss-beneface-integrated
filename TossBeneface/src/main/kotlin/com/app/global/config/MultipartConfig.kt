package com.app.global.config

import jakarta.servlet.MultipartConfigElement
import org.springframework.boot.web.servlet.MultipartConfigFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.util.unit.DataSize
import org.springframework.web.multipart.MultipartResolver
import org.springframework.web.multipart.support.StandardServletMultipartResolver

@Configuration
class MultipartConfig {
    @Bean
    fun multipartResolver(): MultipartResolver {
        return StandardServletMultipartResolver()
    }

    @Bean
    fun multipartConfigElement(): MultipartConfigElement {
        val factory = MultipartConfigFactory()

        // 파일 크기 제한 설정
        factory.setFileSizeThreshold(DataSize.ofMegabytes(2 * 1024L)) // 2KB
        factory.setMaxFileSize(DataSize.ofMegabytes(10)) // 단일 파일 최대 크기
        factory.setMaxRequestSize(DataSize.ofMegabytes(100)) // 전체 요청 최대 크기
        // 임시 저장 위치 설정
        factory.setLocation("src/main/resources/filetmp")

        return factory.createMultipartConfig()
    }
}
