package com.app.api.faceregistration.service

import com.app.api.faceregistration.dto.FaceRegistrationResponse
import com.app.api.file.service.FileUploadService
import com.app.domain.faceregistration.entity.FaceRegistration
import com.app.domain.faceregistration.repository.FaceRegistrationRepository
import com.app.domain.member.repository.MemberRepository
import com.app.global.error.ErrorCode
import com.app.global.error.exception.EntityNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.multipart.MultipartFile

@Service
@Transactional(readOnly = true)
class FaceRegistrationService(
    private val fileUploadService: FileUploadService,
    private val faceRegistrationRepository: FaceRegistrationRepository,
    private val memberRepository: MemberRepository
) {

    @Transactional
    fun uploadFace(memberId: Long, file: MultipartFile): FaceRegistrationResponse {
        val member = memberRepository.findById(memberId)
            .orElseThrow { EntityNotFoundException(ErrorCode.MEMBER_NOT_EXIST) }

        val imageUrl = fileUploadService.uploadFile(file)
        val savedEntity = faceRegistrationRepository.save(FaceRegistration(member = member, imageUrl = imageUrl))
        return FaceRegistrationResponse.from(savedEntity)
    }

    fun getFacesByMember(memberId: Long): List<FaceRegistrationResponse> {
        return faceRegistrationRepository.findByMemberMemberId(memberId)
            .map { FaceRegistrationResponse.from(it) }
    }

    fun getLastFiveFaces(): List<FaceRegistrationResponse> {
        return faceRegistrationRepository.findTop5ByOrderByIdDesc()
            .map { FaceRegistrationResponse.from(it) }
    }

    fun getAllFaces(): List<FaceRegistrationResponse> {
        return faceRegistrationRepository.findAll()
            .map { FaceRegistrationResponse.from(it) }
    }

    fun getFaceById(id: Long): FaceRegistrationResponse {
        val entity = faceRegistrationRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorCode.FACE_NOT_FOUND) }
        return FaceRegistrationResponse.from(entity)
    }

    @Transactional
    fun deleteFace(id: Long) {
        val entity = faceRegistrationRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorCode.FACE_NOT_FOUND) }

        fileUploadService.deleteFileFromS3(entity.imageUrl)
        faceRegistrationRepository.delete(entity)
    }
}
