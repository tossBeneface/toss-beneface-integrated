package com.app.api.flow.service

import com.app.api.flow.dto.FlowDto
import com.app.api.flow.repository.FlowRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FlowServiceImpl(
    private val flowRepository: FlowRepository
) : FlowService {

    @Transactional(readOnly = true)
    override fun getFlow(): FlowDto {
        val flow = flowRepository.findTopByOrderByCreatedAtDesc()
            .orElseThrow { IllegalArgumentException("데이터가 존재하지 않습니다.") }

        return FlowDto(
            storeGenderScript = flow.storeAgeScript,
            storeAgeScript = flow.storeAgeScript,
            storeDayScript = flow.storeDayScript,
            storeTimeScript = flow.storeTimeScript,
            districtGenderScript = flow.districtGenderScript,
            districtAgeScript = flow.districtAgeScript,
            districtDayScript = flow.districtDayScript,
            districtTimeScript = flow.districtTimeScript,
            createAt = flow.createdAt
        )
    }
}
