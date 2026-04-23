package com.app.domain.qnaboard.entity

import com.app.domain.common.BaseEntity
import com.app.domain.member.entity.Member
import com.app.domain.qnaboard.constant.ContentStatus
import jakarta.persistence.*

@Entity
class QnaBoard(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val qnaBoardId: Long? = null,

    @Column(nullable = false, length = 255)
    var title: String,

    @Column(nullable = false, columnDefinition = "TEXT")
    var content: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    var contentStatus: ContentStatus,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "memberId", nullable = false)
    var member: Member,

    @OneToMany(mappedBy = "qnaBoard", cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<Comment> = mutableListOf(),

    @OneToMany(mappedBy = "qnaBoard", cascade = [CascadeType.ALL], orphanRemoval = true)
    var attachments: MutableList<Attachment> = mutableListOf()
) : BaseEntity() {

    fun update(newTitle: String?, newContent: String?) {
        newTitle?.let { if (it.isNotEmpty()) this.title = it }
        newContent?.let { if (it.isNotEmpty()) this.content = it }
    }

    fun changeStatus(newStatus: ContentStatus) {
        this.contentStatus = newStatus
    }

    fun setMemberRelational(member: Member) {
        this.member = member
        if (member.contents.contains(this).not()) {
            member.addQnaBoard(this)
        }
    }

    fun addComment(comment: Comment) {
        comments.add(comment)
        comment.qnaBoard = this
        comment.member?.let { it.addComment(comment) }
    }

    fun removeComment(comment: Comment) {
        comments.remove(comment)
        comment.qnaBoard = null
    }

    fun addAttachment(attachment: Attachment) {
        attachments.add(attachment)
        attachment.qnaBoard = this
    }

    fun addAttachments(attachments: List<Attachment>) {
        attachments.forEach { addAttachment(it) }
    }

    fun removeAttachment(attachment: Attachment) {
        attachments.remove(attachment)
        attachment.qnaBoard = null
    }
}
