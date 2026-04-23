package com.app.domain.qnaboard.service

import com.app.api.file.service.FileUploadService
import com.app.domain.qnaboard.constant.FileStatus
import com.app.domain.qnaboard.entity.Attachment
import com.app.domain.qnaboard.entity.QnaBoard
import com.app.domain.qnaboard.repository.AttachmentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional
class AttachmentService(
    private val fileUploadService: FileUploadService,
    private val attachmentRepository: AttachmentRepository
) {
    fun saveAttachments(files: List<MultipartFile>?, qnaBoard: QnaBoard): List<Attachment> {
        if (files.isNullOrEmpty()) {
            return emptyList()
        }

        return files.map { file ->
            val attachment = createAttachment(file, qnaBoard)
            qnaBoard.addAttachment(attachment)
            attachment
        }
    }

    private fun createAttachment(file: MultipartFile, qnaBoard: QnaBoard): Attachment {
        val fileUrl = fileUploadService.uploadFile(file)

        val attachment = Attachment(
            qnaBoard = qnaBoard,
            url = fileUrl,
            filePath = file.originalFilename,
            fileType = file.contentType ?: "unknown",
            fileStatus = FileStatus.ACTIVATE
        )

        return attachmentRepository.save(attachment)
    }

    @Transactional
    fun deleteAttachmentsByQnaBoard(qnaBoard: QnaBoard) {
        // 기존 Java 코드에 findByQnaBoard가 있을 텐데 Repository에 추가 정의 필요
        // 일단 findAll().filter() 또는 Repository 인터페이스 확장을 고려
    }
}
