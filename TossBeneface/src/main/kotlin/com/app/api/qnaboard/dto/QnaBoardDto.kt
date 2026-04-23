package com.app.api.qnaboard.dto

import com.app.domain.qnaboard.constant.ContentStatus
import com.app.domain.qnaboard.entity.QnaBoard
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime

class QnaBoardDto {

    data class Request(
        @field:Schema(description = "작성자 ID", example = "1", required = true)
        @field:NotNull(message = "작성자 ID는 필수 항목입니다.")
        val memberId: Long,

        @field:Schema(description = "제목", example = "상권리포트에 대해 궁금한게 있어요", required = true)
        @field:NotBlank(message = "제목은 필수 항목입니다.")
        @field:Size(max = 255, message = "제목은 최대 255자까지 입력할 수 있습니다.")
        val title: String,

        @field:Schema(description = "내용", example = "상권리포트 차트를 보면 이런 이런 내용이 있는데 ...", required = true)
        @field:NotBlank(message = "내용은 필수 항목입니다.")
        val content: String,

        @field:Schema(description = "첨부파일 리스트", required = false)
        val files: List<MultipartFile>? = null
    )

    data class Response(
        @field:Schema(description = "게시글id", example = "7", required = true)
        val qnaBoardId: Long,

        @field:Schema(description = "제목", example = "상권리포트에 대해 궁금한게 있어요", required = true)
        val title: String,

        @field:Schema(description = "내용", example = "상권리포트 차트를 보면 이런 이런 내용이 있는데 ...", required = true)
        val content: String,

        @field:Schema(description = "작성자 이름", example = "백사장", required = true)
        val memberName: String,

        @field:Schema(description = "게시글상태", example = "ACTIVATE/DEACTIVATE", required = true)
        val contentStatus: ContentStatus,

        @field:Schema(description = "첨부파일 URL 리스트", required = true)
        val attachmentUrls: List<String>
    ) {
        companion object {
            fun of(qnaBoard: QnaBoard, attachmentUrls: List<String>, memberName: String): Response {
                return Response(
                    qnaBoardId = qnaBoard.qnaBoardId ?: 0L,
                    title = qnaBoard.title,
                    content = qnaBoard.content,
                    memberName = memberName,
                    attachmentUrls = attachmentUrls,
                    contentStatus = qnaBoard.contentStatus
                )
            }
        }
    }

    data class UpdateRequest(
        @field:Schema(description = "제목", example = "상권리포트에 대해 궁금한게 있어요", required = true)
        val title: String,
        @field:Schema(description = "내용", example = "상권리포트 차트를 보면 이런 이런 내용이 있는데 ...", required = true)
        val content: String,
        @field:Schema(description = "첨부파일 URL 리스트", required = true)
        val attachmentUrls: List<String>? = null
    )

    data class Summary(
        val id: Long,
        val title: String,
        val authorName: String,
        val createdAt: LocalDateTime,
        val hasComments: Boolean
    )
}
