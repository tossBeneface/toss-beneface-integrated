package com.app.domain.qnaboard.repository

import com.app.api.qnaboard.dto.QnaBoardDto
import com.app.domain.member.entity.QMember
import com.app.domain.qnaboard.constant.ContentStatus
import com.app.domain.qnaboard.entity.QAttachment
import com.app.domain.qnaboard.entity.QQnaBoard
import com.app.domain.qnaboard.entity.QnaBoard
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository

@Repository
class QnaBoardRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : QnaBoardRepositoryCustom {

    private fun findQnaBoard(qnaBoardId: Long, onlyActive: Boolean): QnaBoard? {
        val qnaBoard = QQnaBoard.qnaBoard
        val attachment = QAttachment.attachment
        val member = QMember.member

        var query = queryFactory.selectFrom(qnaBoard)
            .leftJoin(qnaBoard.attachments, attachment).fetchJoin()

        if (!onlyActive) {
            query.leftJoin(qnaBoard.member, member).fetchJoin()
        }

        query.where(qnaBoard.qnaBoardId.eq(qnaBoardId))

        if (onlyActive) {
            query.where(qnaBoard.contentStatus.eq(ContentStatus.ACTIVATE))
        }

        return query.fetchOne()
    }

    override fun findQnaBoardWithDetails(qnaBoardId: Long): QnaBoard? {
        return findQnaBoard(qnaBoardId, false)
    }

    override fun updateQnaBoard(qnaBoardId: Long, updateRequest: QnaBoardDto.UpdateRequest): QnaBoard? {
        val qnaBoard = findQnaBoard(qnaBoardId, true) ?: throw IllegalArgumentException("존재하지 않거나 비활성화된 게시글입니다.")
        
        qnaBoard.update(updateRequest.title, updateRequest.content)
        return qnaBoard
    }

    override fun findAllQnaBoardsWithDetails(): List<QnaBoard> {
        val qnaBoard = QQnaBoard.qnaBoard
        val member = QMember.member

        return queryFactory.selectFrom(qnaBoard)
            .leftJoin(qnaBoard.member, member).fetchJoin()
            .where(qnaBoard.contentStatus.eq(ContentStatus.ACTIVATE))
            .fetch()
    }
}
