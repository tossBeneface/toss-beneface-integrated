package com.app.config

import com.app.api.addBasic.BasicMenu
import com.app.api.addBasic.BasicMenuRepository
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component

@Component
class BasicMenuDataInitializer(
    private val basicMenuRepository: BasicMenuRepository
) : CommandLineRunner {

    override fun run(vararg args: String?) {
        insertMenuIfNotExists("카페A", "아메리카노", "https://example.com/americano.jpg", 4500.0, 50)
        insertMenuIfNotExists("카페A", "카페라떼", "https://example.com/cafelatte.jpg", 5000.0, 30)
        insertMenuIfNotExists("카페B", "바닐라라떼", "https://example.com/vanillalatte.jpg", 5500.0, 40)
        insertMenuIfNotExists("카페B", "카라멜 마키아토", "https://example.com/caramelmacchiato.jpg", 6000.0, 25)
        println("✅ 기본 메뉴 데이터 삽입 완료!")
    }

    private fun insertMenuIfNotExists(cafe: String, menu: String, img: String, price: Double, stock: Int) {
        val existingMenu = basicMenuRepository.findByCafeAndMenu(cafe, menu)
        if (existingMenu.isEmpty) {
            basicMenuRepository.save(BasicMenu(cafe, menu, img, price, stock))
            println("✅ 메뉴 추가됨: $menu ($cafe)")
        } else {
            println("⚠ 이미 존재하는 메뉴: $menu ($cafe)")
        }
    }
}
