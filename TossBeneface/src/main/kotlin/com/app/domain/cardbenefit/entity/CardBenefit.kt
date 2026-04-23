package com.app.domain.cardbenefit.entity

import com.app.domain.card.entity.Card
import com.app.domain.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "CARD_BENEFIT")
class CardBenefit(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "card_name")
    var cardName: String? = null,

    @Column(name = "card_company")
    var cardCompany: String? = null,

    @Column(name = "summary")
    var summary: String? = null,

    @Column(name = "Shop")
    var shop: String? = null,

    @Column(name = "Benefit")
    var benefit: Int = 0,

    @Column(name = "Limit_once")
    var limitOnce: Int = 0,

    @Column(name = "Limit_month")
    var limitMonth: Int = 0,

    @Column(name = "min_pay")
    var minPay: Int = 0,

    @Column(name = "min_per")
    var minPer: Int = 0,

    @Column(name = "monthly")
    var monthly: Int = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    var card: Card
) : BaseEntity()
