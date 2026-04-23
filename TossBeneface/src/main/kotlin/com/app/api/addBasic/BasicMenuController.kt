package com.app.api.addBasic

import com.app.api.file.service.FileUploadService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

@RestController
@RequestMapping("/api/basic-menu")
class BasicMenuController(
    private val basicMenuRepository: BasicMenuRepository,
    private val fileUploadService: FileUploadService
) {

    @PostMapping("/upload")
    fun uploadMenuCsv(@RequestParam("file") file: MultipartFile): String {
        return try {
            val reader = BufferedReader(InputStreamReader(file.inputStream, StandardCharsets.UTF_8))
            val menuList = reader.lineSequence()
                .drop(1)
                .map { line ->
                    val data = line.split(",")
                    val cafe = data[0].trim()
                    val menu = data[1].trim()
                    val price = data[2].trim().toDouble()
                    val stock = data[3].trim().toInt()
                    val img = if (data.size > 4 && data[4].trim().isNotEmpty()) null else null
                    BasicMenu(cafe, menu, img, price, stock)
                }
                .toList()

            basicMenuRepository.saveAll(menuList)
            "✅ 메뉴 업로드 완료!"
        } catch (e: Exception) {
            "❌ CSV 업로드 실패: ${e.message}"
        }
    }

    @GetMapping("/cafe/{cafeName}")
    fun getMenusByCafe(@PathVariable cafeName: String): List<BasicMenu> {
        return basicMenuRepository.findByCafe(cafeName)
    }
}
