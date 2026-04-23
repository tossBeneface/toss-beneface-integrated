package com.app.domain.qnaboard.repository

import com.app.api.qnaboard.dto.QnaBoardDto
import com.app.domain.qnaboard.entity.QnaBoard

interface QnaBoardRepositoryCustom {
    fun findAllQnaBoardsWithDetails(): List<QnaBoard>
    fun findQnaBoardWithDetails(qnaBoardId: Long): QnaBoard?
    fun updateQnaBoard(qnaBoardId: Long, updateRequest: QnaBoardDto.UpdateRequest): QnaBoard?
}
