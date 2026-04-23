package com.app.api.addProduct

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.Optional

interface ProductRepository : JpaRepository<Product, Long> {
    fun findByCafe(cafe: String): List<Product>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.cafe = :cafe and p.menu = :menu")
    fun findByCafeAndMenuWithPessimisticLock(
        @Param("cafe") cafe: String,
        @Param("menu") menu: String
    ): Optional<Product>
}
