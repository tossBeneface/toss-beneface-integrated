package com.app.api.cardbenefit.service

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.net.InetSocketAddress
import java.net.Socket

@SpringBootTest
@ActiveProfiles("test")
class CardBenefitGrpcClientTest {

    @Autowired
    private lateinit var cardBenefitGrpcClient: CardBenefitGrpcClient

    @Test
    fun `FastAPI gRPC 서버 호출 통합 테스트`() {
        assumeTrue(isGrpcServerAvailable(), "FastAPI gRPC server is not running on localhost:50051")

        // Given
        val memberId = 1L
        val storeName = "스타벅스"
        val category = "CAFE"
        val amount = 10000

        // When
        // FastAPI gRPC 서버가 떠 있어야 합니다.
        val response = cardBenefitGrpcClient.analyzeBestBenefit(memberId, storeName, category, amount)

        // Then
        assertNotNull(response)
        println("Best Card: ${response.bestCardName}")
        println("Total Benefit: ${response.totalPotentialBenefit}")
        
        assertEquals("Toss Beneface Card", response.bestCardName)
        // 10000 * (50% base + 5% bonus for 3rd visit) = 5500
        assertEquals(5500, response.totalPotentialBenefit)
        assertEquals(2, response.allOptionsCount)
    }

    private fun isGrpcServerAvailable(): Boolean {
        return runCatching {
            Socket().use { socket ->
                socket.connect(InetSocketAddress("127.0.0.1", 50051), 500)
            }
            true
        }.getOrDefault(false)
    }
}
