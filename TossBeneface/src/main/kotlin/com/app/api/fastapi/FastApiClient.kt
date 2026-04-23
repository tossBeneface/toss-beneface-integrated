package com.app.api.fastapi

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name = "fastApiClient", url = "\${fast.api.host}")
interface FastApiClient {
    @GetMapping("/hospital_by_module")
    fun getHospital(
        @RequestParam("request") request: String,
        @RequestParam("latitude") latitude: Double,
        @RequestParam("longitude") longitude: Double
    ): List<HospitalResponse>
}
