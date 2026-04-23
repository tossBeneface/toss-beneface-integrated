package com.app.global.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

object JsonUtils {
    private val mapper: ObjectMapper = jacksonObjectMapper()

    fun <T> deserialize(json: String?, clazz: Class<T>): T? {
        if (json == null) return null

        return try {
            mapper.readValue(json, clazz)
        } catch (e: JsonProcessingException) {
            System.err.println("JSON deserialization error: ${e.message}")
            null
        }
    }
}
