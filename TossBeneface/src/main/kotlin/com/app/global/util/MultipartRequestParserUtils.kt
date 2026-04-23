package com.app.global.util

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

object MultipartRequestParserUtils {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    /**
     * JSON 데이터를 특정 DTO 클래스로 변환
     *
     * @param request     MultipartHttpServletRequest
     * @param parameter   JSON 데이터의 파라미터 이름
     * @param valueType   변환할 DTO 클래스
     * @param <T>         DTO 타입
     * @return 변환된 DTO 객체
     * @throws IllegalArgumentException 잘못된 JSON 형식일 경우 예외 발생
     */
    fun <T> parseJson(request: MultipartHttpServletRequest, parameter: String, valueType: Class<T>): T {
        val json = request.getParameter(parameter)

        return try {
            objectMapper.readValue(json, valueType)
        } catch (e: JsonProcessingException) {
            throw IllegalArgumentException("잘못된 JSON 형식입니다.", e)
        }
    }

    /**
     * 파일 데이터를 추출
     *
     * @param request MultipartHttpServletRequest
     * @param key     파일의 파라미터 이름
     * @return MultipartFile 리스트
     */
    fun parseFiles(request: MultipartHttpServletRequest, key: String): List<MultipartFile> {
        return request.getFiles(key)
    }
}
