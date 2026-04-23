package com.app.api.card.service

import com.app.api.card.dto.OcrCardRequestDto
import com.app.api.card.dto.OcrCardResponseDto
import com.app.api.card.repository.CardBinRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

/*
@Service
class OcrCardService(
    private val cardBinRepository: CardBinRepository
) {

    @Value("\${fastapi.url}")
    private lateinit var fastApiUrl: String

    fun processCard(dto: OcrCardRequestDto): OcrCardResponseDto {
        val restTemplate = RestTemplate()
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val requestEntity = HttpEntity(dto.image!!.resource, headers)

        val responseEntity = restTemplate.postForEntity(fastApiUrl, requestEntity, String::class.java)
        if (responseEntity.statusCode != HttpStatus.OK) {
            throw RuntimeException("FastAPI 호출 실패")
        }

        val objectMapper = ObjectMapper()
        val responseBody = try {
            objectMapper.readTree(responseEntity.body)
        } catch (e: Exception) {
            throw RuntimeException("FastAPI 응답 파싱 실패")
        }

        val cardNumber = responseBody.get("card_number").asText()
        val cardDetected = responseBody.get("card_detected").asBoolean()
        if (!cardDetected) {
            return OcrCardResponseDto(false, null, null, null, null, null, null, null)
        }

        val cardBinEntity = cardBinRepository.findByCardNumber(cardNumber)
            .orElseThrow { RuntimeException("카드 BIN 정보가 존재하지 않습니다.") }

        return OcrCardResponseDto(
            true,
            cardNumber,
            cardBinEntity.card.cardName,
            cardBinEntity.card.cardCompany,
            cardBinEntity.card.cardImage,
            responseBody.get("date_info").asText(),
            responseBody.get("cvc_info").asText(),
            responseBody.get("other_text").asText()
        )
    }
}
*/
