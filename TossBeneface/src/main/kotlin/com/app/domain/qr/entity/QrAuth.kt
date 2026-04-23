package com.app.domain.qr.entity

import com.app.domain.common.BaseEntity
import com.app.domain.member.entity.Member
import jakarta.persistence.*

@Entity
class QrAuth(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(nullable = false)
    var nonce: String = "",

    @Column(nullable = false)
    var authenticated: Boolean = false,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    var member: Member? = null
) : BaseEntity()
