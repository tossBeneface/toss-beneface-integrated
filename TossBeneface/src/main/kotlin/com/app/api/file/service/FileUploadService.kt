package com.app.api.file.service

import com.app.domain.qnaboard.entity.Attachment
import com.app.domain.qnaboard.repository.AttachmentRepository
import com.app.global.util.FileUtils
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import java.io.IOException
import java.util.*

@Service
@Transactional
class FileUploadService(
    private val s3Client: S3Client,
    private val attachmentRepository: AttachmentRepository
) {
    private val bucketName = "ai0310bucket"

    fun uploadFile(file: MultipartFile): String {
        FileUtils.validateFileExtension(file)

        val fileName = generateUniqueFileName(file.originalFilename)

        return try {
            s3Client.putObject(
                { builder -> builder.bucket(bucketName).key(fileName) },
                RequestBody.fromInputStream(file.inputStream, file.size)
            )
            "https://$bucketName.s3.ap-southeast-2.amazonaws.com/$fileName"
        } catch (e: IOException) {
            throw RuntimeException("파일 업로드 중 오류가 발생했습니다.", e)
        }
    }

    fun updateFile(fileId: Long, file: MultipartFile): Attachment {
        val existingAttachment = attachmentRepository.findById(fileId)
            .orElseThrow { IllegalArgumentException("첨부파일을 찾을 수 없습니다.") }

        val newFileUrl = uploadFile(file)
        existingAttachment.updateFile(newFileUrl)
        return attachmentRepository.save(existingAttachment)
    }

    fun updateMultipleFiles(fileIds: List<Long>, files: List<MultipartFile>): List<Attachment> {
        if (fileIds.size != files.size) {
            throw IllegalArgumentException("파일 ID와 업로드된 파일 개수가 일치하지 않습니다.")
        }

        val updatedAttachments = mutableListOf<Attachment>()

        for (i in files.indices) {
            val fileId = fileIds[i]
            val file = files[i]
            updatedAttachments.add(updateFile(fileId, file))
        }

        return updatedAttachments
    }

    fun deleteFileFromS3(fileUrl: String) {
        val fileName = FileUtils.extractFileNameFromUrl(fileUrl)
        try {
            s3Client.deleteObject { builder -> builder.bucket(bucketName).key(fileName) }
        } catch (e: Exception) {
            throw RuntimeException("S3 파일 삭제 중 오류가 발생했습니다.", e)
        }
    }

    private fun generateUniqueFileName(originalFilename: String?): String {
        return "${UUID.randomUUID()}_$originalFilename"
    }
}
