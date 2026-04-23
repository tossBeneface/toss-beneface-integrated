package com.app.domain.qnaboard.service

import com.app.api.qnaboard.dto.QnaBoardDto
import com.app.domain.qnaboard.constant.ContentStatus
import com.app.domain.qnaboard.entity.QnaBoard
import com.app.domain.qnaboard.repository.QnaBoardRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class QnaBoardService(
    private val qnaBoardRepository: QnaBoardRepository
) {
    fun createQnaBoard(qnaBoard: QnaBoard): QnaBoard {
        return qnaBoardRepository.save(qnaBoard)
    }

    @Transactional(readOnly = true)
    fun findAllQnaBoardsWithDetails(): List<QnaBoard> {
        return qnaBoardRepository.findAllQnaBoardsWithDetails()
    }

    @Transactional(readOnly = true)
    fun findQnaBoardWithDetails(qnaBoardId: Long): QnaBoard {
        return qnaBoardRepository.findQnaBoardWithDetails(qnaBoardId)
            ?: throw IllegalArgumentException("존재하지 않는 게시글입니다.")
    }

    fun updateQnaBoard(qnaBoardId: Long, updateRequest: QnaBoardDto.UpdateRequest): QnaBoardDto.Response {
        val updatedQnaBoard = qnaBoardRepository.updateQnaBoard(qnaBoardId, updateRequest)
            ?: throw IllegalArgumentException("게시글 수정 중 오류가 발생했습니다.")

        val attachmentUrls = updatedQnaBoard.attachments.map { it.url }

        return QnaBoardDto.Response.of(
            updatedQnaBoard,
            attachmentUrls,
            updatedQnaBoard.member.memberName ?: "알 수 없음"
        )
    }

    fun deleteQnaBoard(qnaBoard: QnaBoard) {
        qnaBoard.changeStatus(ContentStatus.DEACTIVATE)
    }
}
