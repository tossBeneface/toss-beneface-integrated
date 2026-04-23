package com.app.api.addBasic

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface BasicMenuRepository : JpaRepository<BasicMenu, Long> {
    fun findByCafeAndMenu(cafe: String, menu: String): Optional<BasicMenu>
    fun findByCafe(cafe: String): List<BasicMenu>
}
