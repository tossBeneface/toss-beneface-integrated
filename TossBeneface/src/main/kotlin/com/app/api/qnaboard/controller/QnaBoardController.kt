package com.app.api.qnaboard.controller

import com.app.api.qnaboard.dto.QnaBoardDto
import com.app.api.qnaboard.service.QnaBoardInfoService
import com.app.global.util.JsonUtils
import com.app.global.util.MultipartRequestParserUtils
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.multipart.MultipartHttpServletRequest

@Tag(name = "qnaboard", description = "qna게시판 게시글 API")
@RestController
@RequestMapping("/api/qnaboard")
class QnaBoardController(
    private val qnaBoardInfoService: QnaBoardInfoService
) {

    @GetMapping("/test")
    fun testInterceptor(): ResponseEntity<String> {
        return ResponseEntity.ok("인터셉터가 동작합니다!")
    }

    @Operation(summary = "게시글 작성 API")
    @PostMapping("/create")
    fun createQnaBoard(
        @RequestPart("requestDto") requestDtoJson: String,
        @RequestPart(value = "files", required = false) files: List<MultipartFile>?
    ): ResponseEntity<Long> {
        val requestDto = JsonUtils.deserialize(requestDtoJson, QnaBoardDto.Request::class.java)
            ?: throw IllegalArgumentException("잘못된 요청 데이터입니다.")
        
        val finalRequestDto = if (files != null) {
            requestDto.copy(files = files)
        } else {
            requestDto
        }

        val qnaBoardId = qnaBoardInfoService.createQnaBoard(finalRequestDto)
        return ResponseEntity.ok(qnaBoardId)
    }

    @Operation(summary = "게시글 상세 API")
    @GetMapping("/{qnaBoardId}")
    fun getQnaBoard(@PathVariable("qnaBoardId") qnaBoardId: Long): ResponseEntity<QnaBoardDto.Response> {
        val responseDto = qnaBoardInfoService.getQnaBoardById(qnaBoardId)
        return ResponseEntity.ok(responseDto)
    }

    @Operation(summary = "게시글 목록 API")
    @GetMapping
    fun getAllQnaBoards(): ResponseEntity<List<QnaBoardDto.Summary>> {
        val qnaBoardSummaries = qnaBoardInfoService.getQnaBoardSummaries()
        return ResponseEntity.ok(qnaBoardSummaries)
    }

    @Operation(summary = "게시글 수정 API")
    @PutMapping("/{qnaBoardId}")
    fun updateQnaBoard(
        @PathVariable("qnaBoardId") qnaBoardId: Long,
        request: MultipartHttpServletRequest
    ): ResponseEntity<Long> {
        val updateRequest = MultipartRequestParserUtils.parseJson(request, "requestDto", QnaBoardDto.UpdateRequest::class.java)
        val files = MultipartRequestParserUtils.parseFiles(request, "files")

        val updatedQnaBoardId = qnaBoardInfoService.updateQnaBoard(qnaBoardId, updateRequest, files)
        return ResponseEntity.ok(updatedQnaBoardId)
    }

    @Operation(summary = "게시글 삭제 API")
    @DeleteMapping("/{qnaBoardId}")
    fun deleteQnaBoard(@PathVariable("qnaBoardId") qnaBoardId: Long): ResponseEntity<Void> {
        qnaBoardInfoService.deleteQnaBoard(qnaBoardId)
        return ResponseEntity.ok().build()
    }
}
