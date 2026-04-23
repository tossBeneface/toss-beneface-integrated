package com.app.api.flow.service

import com.app.api.flow.dto.FlowDto

interface FlowService {
    fun getFlow(): FlowDto
}
