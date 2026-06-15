package com.app.domain.member.entity

import com.app.domain.common.BaseEntity
import com.app.domain.member.constant.Gender
import com.app.domain.member.constant.MemberStatus
import com.app.domain.member.constant.Role
import com.app.domain.member.model.MemberAuthority
import com.app.domain.member.model.MemberIdentity
import com.app.domain.member.model.MemberOnboarding
import com.app.domain.member.model.MemberProfile
import com.app.domain.qnaboard.entity.Comment
import com.app.domain.qnaboard.entity.QnaBoard
import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import jakarta.persistence.*

@Entity
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var memberId: Long? = null,

    @Column(unique = true, length = 50, nullable = false)
    var email: String,

    @Column(nullable = true, length = 200)
    var password: String?,

    @Column(nullable = false, length = 20)
    var memberName: String,

    @Column(nullable = false, length = 20)
    var phoneNumber: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var gender: Gender,

    @Column(nullable = true, length = 200)
    var profileImg: String? = null,

    @Column(nullable = true, columnDefinition = "INT DEFAULT 1000000")
    var budget: Int? = 10000000,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    var role: Role,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    var memberStatus: MemberStatus,

    @Column(nullable = true, length = 20)
    var socialType: String? = null,

    @Column(nullable = true, length = 255)
    var socialId: String? = null,

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    var contents: MutableList<QnaBoard> = mutableListOf(),

    @OneToMany(mappedBy = "member", cascade = [CascadeType.ALL], orphanRemoval = true)
    var comments: MutableList<Comment> = mutableListOf()
) : BaseEntity() {

    // Java의 @Builder 호환성을 위해 부생성자 또는 기본 생성자 활용
    constructor(
        email: String,
        password: String,
        memberName: String,
        phoneNumber: String,
        gender: Gender,
        profileImg: String?,
        budget: Int?,
        role: Role,
        memberStatus: MemberStatus,
        socialType: String? = null,
        socialId: String? = null
    ) : this(
        null, email, password, memberName, phoneNumber, gender, profileImg,
        budget ?: 10000000, role, memberStatus, socialType, socialId
    )

    companion object {
        fun registerLocal(
            identity: MemberIdentity,
            profile: MemberProfile,
            authority: MemberAuthority,
            onboarding: MemberOnboarding
        ): Member {
            return Member(
                email = identity.email,
                password = identity.password,
                memberName = profile.memberName,
                phoneNumber = profile.phoneNumber,
                gender = profile.gender,
                profileImg = profile.profileImg,
                budget = onboarding.initialBudget,
                role = authority.role,
                memberStatus = onboarding.status,
                socialType = null,
                socialId = null
            )
        }

        fun registerSocial(
            identity: MemberIdentity,
            profile: MemberProfile,
            onboarding: MemberOnboarding = MemberOnboarding()
        ): Member {
            return Member(
                email = identity.email,
                password = null,
                memberName = profile.memberName,
                phoneNumber = profile.phoneNumber,
                gender = profile.gender,
                profileImg = profile.profileImg,
                budget = onboarding.initialBudget,
                role = Role.USER,
                memberStatus = onboarding.status,
                socialType = identity.socialType,
                socialId = identity.socialId
            )
        }

    }

    fun addQnaBoard(qnaBoard: QnaBoard) {
        if (!contents.contains(qnaBoard)) {
            contents.add(qnaBoard)
        }
        qnaBoard.member = this
    }

    fun addComment(comment: Comment) {
        if (!comments.contains(comment)) {
            comments.add(comment)
        }
        comment.member = this
    }

    fun updateMemberStatus(memberStatus: MemberStatus) {
        this.memberStatus = memberStatus
    }

    fun connectSocialIdentity(socialType: String, socialId: String, memberName: String) {
        this.socialType = socialType
        this.socialId = socialId
        this.memberName = memberName
    }

    /**
     * 예산 차감 (비즈니스 로직 응집)
     */
    fun checkAndSubtractBudget(amount: Int) {
        validateBudget(amount)
        this.budget = (this.budget ?: 0) - amount
    }

    /**
     * 예산 확인
     */
    private fun validateBudget(amount: Int) {
        if ((this.budget ?: 0) < amount) {
            throw BusinessException(ErrorCode.INSUFFICIENT_BUDGET)
        }
    }
}
