package com.app.api.qr.controller

import com.app.api.qr.service.QrService
import com.app.global.resolver.memberInfo.MemberInfoDto
import com.app.global.resolver.memberInfo.MemberInfo
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/qr")
@CrossOrigin(origins = ["http://localhost:3001"])
class QrController(
    private val qrService: QrService
) {

    @GetMapping("/generate")
    fun generateQR(@MemberInfo memberInfoDto: MemberInfoDto): ResponseEntity<ByteArray> {
        return try {
            val qrImage = qrService.generateQr(memberInfoDto.memberId)
            ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage)
        } catch (e: Exception) {
            ResponseEntity.status(500).build()
        }
    }
}
