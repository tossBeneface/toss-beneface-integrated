package com.app.api.flow.controller

import com.app.api.flow.dto.FlowDto
import com.app.api.flow.service.FlowService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class FlowController(
    private val flowService: FlowService
) {

    @GetMapping("/flow")
    fun getFlow(): ResponseEntity<FlowDto> {
        val flowDto = flowService.getFlow()
        return ResponseEntity.status(HttpStatus.OK).body(flowDto)
    }
}
