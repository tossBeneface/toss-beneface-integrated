package com.app.global.config.web

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.databind.ser.std.StdSerializer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.util.HtmlUtils
import java.io.IOException

/*
@Configuration
class JsonConfig {

    @Bean
    fun objectMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        val module = SimpleModule()
        module.addSerializer(String::class.java, object : StdSerializer<String>(String::class.java) {
            @Throws(IOException::class)
            override fun serialize(value: String, gen: JsonGenerator, provider: SerializerProvider) {
                // HTML 이스케이프 처리
                gen.writeString(HtmlUtils.htmlEscape(value))
            }
        })
        objectMapper.registerModule(module)
        return objectMapper
    }
}
*/
