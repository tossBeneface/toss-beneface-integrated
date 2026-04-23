package com.app.api.faceregistration.controller

import com.app.api.faceregistration.dto.FaceRegistrationResponse
import com.app.api.faceregistration.service.FaceRegistrationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/faces")
class FaceRegistrationController(
    private val faceRegistrationService: FaceRegistrationService
) {

    @PostMapping("/upload")
    fun uploadFace(
        @RequestParam("memberId") memberId: Long,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<FaceRegistrationResponse> {
        val response = faceRegistrationService.uploadFace(memberId, file)
        return ResponseEntity.ok(response)
    }

    @GetMapping("/member/{memberId}")
    fun getFacesByMember(@PathVariable memberId: Long): ResponseEntity<List<FaceRegistrationResponse>> {
        val dtos = faceRegistrationService.getFacesByMember(memberId)
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/last5")
    fun getLastFiveFaces(): ResponseEntity<List<FaceRegistrationResponse>> {
        val dtos = faceRegistrationService.getLastFiveFaces()
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/all")
    fun getAllFaces(): ResponseEntity<List<FaceRegistrationResponse>> {
        val dtos = faceRegistrationService.getAllFaces()
        return ResponseEntity.ok(dtos)
    }

    @GetMapping("/{id}")
    fun getFaceById(@PathVariable id: Long): ResponseEntity<FaceRegistrationResponse> {
        val response = faceRegistrationService.getFaceById(id)
        return ResponseEntity.ok(response)
    }

    @DeleteMapping("/{id}")
    fun deleteFace(@PathVariable id: Long): ResponseEntity<Void> {
        faceRegistrationService.deleteFace(id)
        return ResponseEntity.noContent().build()
    }
}
