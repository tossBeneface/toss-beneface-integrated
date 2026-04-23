package com.app.domain.faceregistration.entity

import com.app.domain.member.entity.Member
import jakarta.persistence.*

@Entity
@Table(name = "face_registration")
class FaceRegistration(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,

    @Column(name = "image_url", nullable = false)
    var imageUrl: String
)
