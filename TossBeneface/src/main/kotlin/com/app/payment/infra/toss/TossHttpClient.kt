package com.app.payment.infra.toss

import com.app.payment.application.dto.TossApiResponse
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.stereotype.Component
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL
import java.nio.charset.StandardCharsets
import java.util.Base64

@Component
class TossHttpClient(
    private val objectMapper: ObjectMapper
) {

    fun postJson(requestData: Any, secretKey: String, urlString: String): TossApiResponse {
        val connection = createConnection(secretKey, urlString)
        return try {
            connection.outputStream.use { outputStream ->
                objectMapper.writeValue(outputStream, requestData)
            }

            readResponse(connection)
        } finally {
            connection.disconnect()
        }
    }

    private fun readResponse(connection: HttpURLConnection): TossApiResponse {
        val statusCode = connection.responseCode
        val responseStream: InputStream? = if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        if (responseStream == null) {
            return TossApiResponse(statusCode, mapOf("error" to "Empty response"))
        }

        val responseBody: Map<String, Any> = responseStream.use { inputStream ->
            objectMapper.readValue(inputStream, object : TypeReference<Map<String, Any>>() {})
        }
        return TossApiResponse(statusCode, responseBody)
    }

    private fun createConnection(secretKey: String, urlString: String): HttpURLConnection {
        val url = URL(urlString)
        return (url.openConnection() as HttpURLConnection).apply {
            val auth = Base64.getEncoder().encodeToString("$secretKey:".toByteArray(StandardCharsets.UTF_8))
            setRequestProperty("Authorization", "Basic $auth")
            setRequestProperty("Content-Type", "application/json")
            requestMethod = "POST"
            doOutput = true
        }
    }
}
