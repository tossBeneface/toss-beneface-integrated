package com.app.payment.infra.toss

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.sun.net.httpserver.HttpServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.net.InetSocketAddress
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.concurrent.atomic.AtomicReference

class TossHttpClientTest {

    private val objectMapper = ObjectMapper()
    private val tossHttpClient = TossHttpClient(objectMapper)
    private lateinit var server: HttpServer

    @BeforeEach
    fun setUp() {
        server = HttpServer.create(InetSocketAddress(0), 0)
        server.start()
    }

    @AfterEach
    fun tearDown() {
        server.stop(0)
    }

    @Test
    fun `posts json request with Toss basic auth and maps success response`() {
        val receivedRequest = AtomicReference<ReceivedRequest>()
        server.createContext("/payments/confirm") { exchange ->
            val requestBody = exchange.requestBody.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
            receivedRequest.set(
                ReceivedRequest(
                    method = exchange.requestMethod,
                    authorization = exchange.requestHeaders.getFirst("Authorization"),
                    contentType = exchange.requestHeaders.getFirst("Content-Type"),
                    body = requestBody
                )
            )

            respond(exchange, 200, """{"paymentKey":"payment-key","status":"DONE"}""")
        }

        val response = tossHttpClient.postJson(
            requestData = mapOf(
                "paymentKey" to "payment-key",
                "orderId" to "order-id",
                "amount" to 10_000
            ),
            secretKey = "test-secret",
            urlString = url("/payments/confirm")
        )

        val request = receivedRequest.get()
        val requestBody = objectMapper.readValue(request.body, object : TypeReference<Map<String, Any>>() {})

        assertEquals("POST", request.method)
        assertEquals("Basic ${basicAuth("test-secret")}", request.authorization)
        assertEquals("application/json", request.contentType)
        assertEquals("payment-key", requestBody["paymentKey"])
        assertEquals("order-id", requestBody["orderId"])
        assertEquals(10_000, requestBody["amount"])
        assertEquals(200, response.statusCode)
        assertEquals("payment-key", response.body["paymentKey"])
        assertEquals("DONE", response.body["status"])
    }

    @Test
    fun `maps Toss error response body`() {
        server.createContext("/payments/confirm") { exchange ->
            respond(exchange, 400, """{"code":"INVALID_REQUEST","message":"invalid amount"}""")
        }

        val response = tossHttpClient.postJson(
            requestData = mapOf("paymentKey" to "payment-key"),
            secretKey = "test-secret",
            urlString = url("/payments/confirm")
        )

        assertEquals(400, response.statusCode)
        assertEquals("INVALID_REQUEST", response.body["code"])
        assertEquals("invalid amount", response.body["message"])
    }

    private fun url(path: String): String {
        return "http://localhost:${server.address.port}$path"
    }

    private fun basicAuth(secretKey: String): String {
        return Base64.getEncoder().encodeToString("$secretKey:".toByteArray(StandardCharsets.UTF_8))
    }

    private fun respond(exchange: com.sun.net.httpserver.HttpExchange, statusCode: Int, responseBody: String) {
        val responseBytes = responseBody.toByteArray(StandardCharsets.UTF_8)
        exchange.responseHeaders.add("Content-Type", "application/json")
        exchange.sendResponseHeaders(statusCode, responseBytes.size.toLong())
        exchange.responseBody.use { it.write(responseBytes) }
    }

    private data class ReceivedRequest(
        val method: String,
        val authorization: String?,
        val contentType: String?,
        val body: String
    )
}
