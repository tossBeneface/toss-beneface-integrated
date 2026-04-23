package com.app.domain.card.entity

import jakarta.persistence.*

@Entity
@Table(name = "card")
class Card(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @Column(name = "card_name", nullable = false)
    val cardName: String,

    @Column(name = "card_company", nullable = false)
    val cardCompany: String,

    @Column(name = "card_image")
    val cardImage: String? = null
)
