package com.app.api.file.controller

import com.app.api.file.service.FileUploadService
import com.app.domain.qnaboard.entity.Attachment
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/files")
class FileUploadController(
    private val fileUploadService: FileUploadService
) {

    @PostMapping("/update/{fileId}")
    fun updateFile(
        @PathVariable fileId: Long,
        @RequestPart("file") file: MultipartFile
    ): ResponseEntity<Attachment> {
        val updatedFile = fileUploadService.updateFile(fileId, file)
        return ResponseEntity.ok(updatedFile)
    }

    @PostMapping("/update-multiple")
    fun updateMultipleFiles(
        @RequestPart("files") files: List<MultipartFile>,
        @RequestParam fileIds: List<Long>
    ): ResponseEntity<List<Attachment>> {
        val updatedFiles = fileUploadService.updateMultipleFiles(fileIds, files)
        return ResponseEntity.ok(updatedFiles)
    }
}
