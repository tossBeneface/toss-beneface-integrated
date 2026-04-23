package com.app.api.qnaboard.service

import com.app.api.qnaboard.dto.QnaBoardDto
import com.app.auth.infra.security.JwtTokenProvider
import com.app.auth.infra.web.BearerTokenResolver
import com.app.domain.member.service.MemberService
import com.app.domain.qnaboard.constant.ContentStatus
import com.app.domain.qnaboard.entity.QnaBoard
import com.app.domain.qnaboard.service.AttachmentService
import com.app.domain.qnaboard.service.QnaBoardService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class QnaBoardInfoService(
    private val qnaBoardService: QnaBoardService,
    private val memberService: MemberService,
    private val attachmentService: AttachmentService,
    private val bearerTokenResolver: BearerTokenResolver,
    private val jwtTokenProvider: JwtTokenProvider
) {
    @Transactional
    fun createQnaBoard(requestDto: QnaBoardDto.Request): Long {
        val token = bearerTokenResolver.resolveFromCurrentRequest() ?: throw IllegalArgumentException("토큰이 존재하지 않습니다.")
        val memberId = jwtTokenProvider.extractMemberId(jwtTokenProvider.parseAccessToken(token))
        val member = memberService.findMemberById(memberId)

        val qnaBoard = QnaBoard(
            title = requestDto.title,
            content = requestDto.content,
            member = member,
            contentStatus = ContentStatus.ACTIVATE
        )
        val savedBoard = qnaBoardService.createQnaBoard(qnaBoard)

        requestDto.files?.let {
            if (it.isNotEmpty()) {
                attachmentService.saveAttachments(it, savedBoard)
            }
        }

        return savedBoard.qnaBoardId ?: 0L
    }

    @Transactional(readOnly = true)
    fun getQnaBoardById(qnaBoardId: Long): QnaBoardDto.Response {
        val qnaBoard = qnaBoardService.findQnaBoardWithDetails(qnaBoardId)
        val attachmentUrls = qnaBoard.attachments.map { it.url }

        return QnaBoardDto.Response.of(qnaBoard, attachmentUrls, qnaBoard.member.memberName ?: "알 수 없음")
    }

    @Transactional(readOnly = true)
    fun getQnaBoardSummaries(): List<QnaBoardDto.Summary> {
        val qnaBoards = qnaBoardService.findAllQnaBoardsWithDetails()

        return qnaBoards.map { qnaBoard ->
            val authorName = qnaBoard.member.memberName ?: "알 수 없음"
            val hasComments = qnaBoard.comments.isNotEmpty()

            QnaBoardDto.Summary(
                id = qnaBoard.qnaBoardId ?: 0L,
                title = qnaBoard.title,
                authorName = authorName,
                createdAt = qnaBoard.createdAt,
                hasComments = hasComments
            )
        }
    }

    @Transactional
    fun updateQnaBoard(qnaBoardId: Long, updateRequest: QnaBoardDto.UpdateRequest, files: List<MultipartFile>?): Long {
        val qnaBoard = qnaBoardService.findQnaBoardWithDetails(qnaBoardId)
        qnaBoard.update(updateRequest.title, updateRequest.content)

        if (!files.isNullOrEmpty()) {
            attachmentService.deleteAttachmentsByQnaBoard(qnaBoard)
            attachmentService.saveAttachments(files, qnaBoard)
        }

        return qnaBoard.qnaBoardId ?: 0L
    }

    fun deleteQnaBoard(qnaBoardId: Long) {
        val qnaBoard = qnaBoardService.findQnaBoardWithDetails(qnaBoardId)
        qnaBoardService.deleteQnaBoard(qnaBoard)
    }
}
