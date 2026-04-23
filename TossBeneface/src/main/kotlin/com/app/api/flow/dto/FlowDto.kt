package com.app.api.flow.dto

import java.time.LocalDateTime

data class FlowDto(
    val storeGenderScript: String? = null,
    val storeAgeScript: String? = null,
    val storeTimeScript: String? = null,
    val storeDayScript: String? = null,
    val districtGenderScript: String? = null,
    val districtAgeScript: String? = null,
    val districtTimeScript: String? = null,
    val districtDayScript: String? = null,
    val createAt: LocalDateTime? = null
)
