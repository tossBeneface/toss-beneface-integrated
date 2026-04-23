package com.app.api.faceregistration.dto

import com.app.domain.faceregistration.entity.FaceRegistration

data class FaceRegistrationResponse(
    val id: Long?,
    val memberId: Long?,
    val imageUrl: String
) {
    companion object {
        fun from(entity: FaceRegistration): FaceRegistrationResponse {
            return FaceRegistrationResponse(
                id = entity.id,
                memberId = entity.member.memberId,
                imageUrl = entity.imageUrl
            )
        }
    }
}
