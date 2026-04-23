package com.app.api.flow.repository

import com.app.domain.analy.entity.DistrictFlow
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface FlowRepository : JpaRepository<DistrictFlow, Long> {
    fun findTopByOrderByCreatedAtDesc(): Optional<DistrictFlow>
}
