package com.app.domain.faceregistration.repository

import com.app.domain.faceregistration.entity.FaceRegistration
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface FaceRegistrationRepository : JpaRepository<FaceRegistration, Long> {
    fun findByImageUrl(imageUrl: String): Optional<FaceRegistration>
    fun findByMemberMemberId(memberId: Long): List<FaceRegistration>
    fun findTop5ByOrderByIdDesc(): List<FaceRegistration>
}
