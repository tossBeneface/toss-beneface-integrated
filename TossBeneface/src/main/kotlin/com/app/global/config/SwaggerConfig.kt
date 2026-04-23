package com.app.global.config

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme as SecuritySchemeModel
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@SecurityScheme(
    name = "Authorization",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
class SwaggerConfig {

    @Bean
    fun customOpenAPI(): OpenAPI {
        val securityRequirement = SecurityRequirement()
            .addList("Authorization")

        return OpenAPI()
            .info(
                Info()
                    .title("API 문서")
                    .description("API에 대한 설명을 제공하는 문서입니다.")
                    .version("1.0")
            )
            .addSecurityItem(securityRequirement)
            .components(
                Components()
                    .addSecuritySchemes(
                        "Authorization",
                        SecuritySchemeModel()
                            .type(SecuritySchemeModel.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
    }
}
