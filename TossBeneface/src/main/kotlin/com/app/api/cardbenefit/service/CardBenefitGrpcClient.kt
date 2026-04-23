package com.app.api.cardbenefit.service

import com.app.grpc.BenefitAnalysisRequest
import com.app.grpc.BenefitAnalysisResponse
import com.app.grpc.CardBenefitServiceGrpc
import net.devh.boot.grpc.client.inject.GrpcClient
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class CardBenefitGrpcClient {

    private val log = LoggerFactory.getLogger(CardBenefitGrpcClient::class.java)

    @GrpcClient("card-benefit-service")
    private lateinit var stub: CardBenefitServiceGrpc.CardBenefitServiceBlockingStub

    fun analyzeBestBenefit(memberId: Long, storeName: String, category: String, amount: Int): BenefitAnalysisResponse {
        log.info("Sending gRPC request to FastAPI for memberId: {}, storeName: {}", memberId, storeName)
        
        val request = BenefitAnalysisRequest.newBuilder()
            .setMemberId(memberId)
            .setStoreName(storeName)
            .setCategory(category)
            .setAmount(amount)
            .build()

        return try {
            val response = stub.analyzeBestBenefit(request)
            log.info("Received gRPC response: Best Card = {}", response.bestCardName)
            response
        } catch (e: Exception) {
            log.error("Failed to call gRPC service", e)
            throw e
        }
    }
}
