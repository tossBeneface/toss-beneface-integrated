package com.app.api.UserDataTest

import com.app.domain.common.BaseEntity
import com.app.domain.member.entity.Member
import com.app.domain.card.entity.Card
import jakarta.persistence.*

@Entity
@Table(name = "user_data_test")
class UserDataTestEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    var member: Member,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    var card: Card,

    @Column(name = "card_name", nullable = false)
    var cardName: String,

    @Column(name = "card_company", nullable = false)
    var cardCompany: String,

    @Column(name = "card_number", nullable = false, length = 20)
    var cardNumber: String,

    @Column(name = "expiry_date", nullable = false, length = 10)
    var expiryDate: String,

    @Column(name = "cvc", nullable = false)
    var cvc: Int = 0,

    @Column(name = "pwd", nullable = false)
    var pwd: Int = 0,

    @Column(name = "last_per", nullable = false)
    var lastPer: Int = 0,

    @Column(name = "now_per", nullable = false)
    var nowPer: Int = 0,

    @Column(name = "card_limit", nullable = false)
    var cardLimit: Int = 0,

    @Column(name = "monthly_split", nullable = false)
    var monthlySplit: Int = 0,

    @Column(name = "accrue_benefit", nullable = false)
    var accrueBenefit: Int = 0,

    @Column(name = "date")
    var date: String? = null,

    @Column(name = "pay_amount")
    var payAmount: Int? = null
) : BaseEntity()
