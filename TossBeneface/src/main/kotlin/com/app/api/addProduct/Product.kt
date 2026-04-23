package com.app.api.addProduct

import jakarta.persistence.*

@Entity
class Product(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    
    var cafe: String? = null,
    var menu: String? = null,
    var img: String? = null,
    var price: Int = 0,
    var stock: Int = 0
)
