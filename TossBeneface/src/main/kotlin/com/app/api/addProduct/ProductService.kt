package com.app.api.addProduct

import com.app.global.config.CacheConfig.Companion.ALL_PRODUCTS_CACHE
import com.app.global.config.CacheConfig.Companion.PRODUCTS_BY_CAFE_CACHE
import org.springframework.cache.annotation.CacheEvict
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ProductService(
    private val productRepository: ProductRepository
) {

    @Cacheable(cacheNames = [ALL_PRODUCTS_CACHE])
    fun getAllProducts(): List<Product> {
        return productRepository.findAll()
    }

    @Cacheable(cacheNames = [PRODUCTS_BY_CAFE_CACHE], key = "#cafe")
    fun getProductsByCafe(cafe: String): List<Product> {
        return productRepository.findByCafe(cafe)
    }

    @Transactional
    @CacheEvict(cacheNames = [ALL_PRODUCTS_CACHE, PRODUCTS_BY_CAFE_CACHE], allEntries = true)
    fun addProduct(product: Product): Product {
        return productRepository.save(product)
    }

    @Transactional
    @CacheEvict(cacheNames = [ALL_PRODUCTS_CACHE, PRODUCTS_BY_CAFE_CACHE], allEntries = true)
    fun updateProduct(id: Long, cafe: String, menu: String, price: Int, stock: Int, img: String?): Product {
        val existingProduct = productRepository.findById(id)
            .orElseThrow { RuntimeException("Product not found") }

        existingProduct.cafe = cafe
        existingProduct.menu = menu
        existingProduct.price = price
        existingProduct.stock = stock

        if (img != null) {
            existingProduct.img = img
        }

        return productRepository.save(existingProduct)
    }

    @Transactional
    @CacheEvict(cacheNames = [ALL_PRODUCTS_CACHE, PRODUCTS_BY_CAFE_CACHE], allEntries = true)
    fun deleteProduct(id: Long) {
        productRepository.deleteById(id)
    }
}
