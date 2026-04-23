package com.app.domain.qnaboard.entity

import com.app.domain.common.BaseEntity
import com.app.domain.qnaboard.constant.FileStatus
import com.app.global.util.FileUtils
import jakarta.persistence.*

@Entity
class Attachment(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val attachmentId: Long? = null,

    @Column(nullable = false, length = 500)
    var url: String,

    @Column(length = 500)
    var filePath: String? = null,

    @Column(nullable = false, length = 50)
    var fileType: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    var fileStatus: FileStatus,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qnaBoardId", nullable = false)
    var qnaBoard: QnaBoard? = null
) : BaseEntity() {

    fun associateWith(qnaBoard: QnaBoard) {
        this.qnaBoard = qnaBoard
        if (qnaBoard.attachments.contains(this).not()) {
            qnaBoard.addAttachment(this)
        }
    }

    fun updateFile(newFileUrl: String) {
        this.url = newFileUrl
        this.filePath = FileUtils.extractFileNameFromUrl(newFileUrl)
        this.fileStatus = FileStatus.ACTIVATE
    }
}
