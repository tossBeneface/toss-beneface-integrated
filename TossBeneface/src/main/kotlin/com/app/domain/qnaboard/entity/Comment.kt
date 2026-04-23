package com.app.domain.qnaboard.entity

import com.app.domain.member.entity.Member
import com.app.domain.qnaboard.constant.CommentStatus
import jakarta.persistence.*

@Entity
class Comment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val commentId: Long? = null,

    @Column(nullable = false, columnDefinition = "TEXT")
    var commentContent: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    var commentStatus: CommentStatus,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qnaBoardId", nullable = false)
    var qnaBoard: QnaBoard? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    var member: Member? = null
) {

    fun associateWith(qnaBoard: QnaBoard) {
        this.qnaBoard = qnaBoard
        if (qnaBoard.comments.contains(this).not()) {
            qnaBoard.addComment(this)
        }
    }

    fun associateWith(member: Member) {
        this.member = member
        if (member.comments.contains(this).not()) {
            member.comments.add(this)
        }
    }
}
