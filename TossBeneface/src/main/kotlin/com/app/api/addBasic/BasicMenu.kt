package com.app.api.addBasic

import jakarta.persistence.*

@Entity
@Table(name = "basic_menu")
class BasicMenu(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var cafe: String,

    var img: String? = null,

    @Column(nullable = false)
    var menu: String,

    @Column(nullable = false)
    var price: Double,

    @Column(nullable = false)
    var stock: Int
) {
    constructor() : this(null, "", null, "", 0.0, 0)
    
    constructor(cafe: String, menu: String, img: String?, price: Double, stock: Int) : this(null, cafe, img, menu, price, stock)
}
