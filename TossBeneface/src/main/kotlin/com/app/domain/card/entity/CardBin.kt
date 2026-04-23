package com.app.domain.card.entity

import jakarta.persistence.*

@Entity
@Table(name = "card_bin")
class CardBin(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "card_bin", nullable = false)
    val cardBin: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "card_id", nullable = false)
    val card: Card
)
