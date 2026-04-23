package com.app.api.addProduct

import com.app.api.file.service.FileUploadService
import com.app.global.error.ErrorCode
import com.app.global.error.exception.BusinessException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/products")
class ProductController(
    private val productService: ProductService,
    private val fileUploadService: FileUploadService
) {
    private val log = LoggerFactory.getLogger(ProductController::class.java)

    @GetMapping
    fun getAllProducts(): List<Product> {
        return productService.getAllProducts()
    }

    @PostMapping(consumes = ["multipart/form-data"])
    fun addProduct(
        @RequestParam("cafe") cafe: String,
        @RequestParam("menu") menu: String,
        @RequestParam("price") price: Int,
        @RequestParam(value = "stock", required = false, defaultValue = "0") stock: Int,
        @RequestPart(value = "img", required = false) imageFile: MultipartFile?
    ): Product {
        val img = if (imageFile != null && !imageFile.isEmpty) {
            fileUploadService.uploadFile(imageFile)
        } else null

        val product = Product(
            cafe = cafe,
            menu = menu,
            price = price,
            stock = stock,
            img = img
        )

        return productService.addProduct(product)
    }

    @PutMapping(value = ["/{id}"], consumes = ["multipart/form-data"])
    fun updateProduct(
        @PathVariable id: Long,
        @RequestParam("cafe") cafe: String,
        @RequestParam("menu") menu: String,
        @RequestParam("price") price: Int,
        @RequestParam(value = "stock", required = false, defaultValue = "0") stock: Int,
        @RequestPart(value = "img", required = false) imageFile: MultipartFile?
    ): Product {
        val img = if (imageFile != null && !imageFile.isEmpty) {
            fileUploadService.uploadFile(imageFile)
        } else null

        return productService.updateProduct(id, cafe, menu, price, stock, img)
    }

    @DeleteMapping("/{id}")
    fun deleteProduct(@PathVariable id: Long) {
        productService.deleteProduct(id)
    }

    @GetMapping("/getmenu")
    fun getMenuByBrand(@RequestParam("brand") brand: String): ResponseEntity<*> {
        if (brand.isBlank()) {
            throw BusinessException(ErrorCode.EMPTY_BRAND)
        }

        val productList = productService.getProductsByCafe(brand)
        if (productList.isEmpty()) {
            return ResponseEntity.ok(
                mapOf(
                    "message" to "해당 브랜드에 메뉴가 없습니다.",
                    "brand" to brand,
                    "menus" to emptyList<Any>()
                )
            )
        }

        val menuDetails = productList.map { product ->
            mapOf(
                "menu" to product.menu,
                "img" to product.img,
                "price" to product.price,
                "stock" to product.stock
            )
        }

        return ResponseEntity.ok(mapOf("brand" to brand, "menus" to menuDetails))
    }
}
