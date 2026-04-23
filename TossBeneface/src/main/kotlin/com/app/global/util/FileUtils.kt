package com.app.global.util

import org.apache.commons.io.FilenameUtils
import org.springframework.web.multipart.MultipartFile

object FileUtils {

    // 업로드 시 허용할 파일 확장자 목록
    private val ALLOWED_EXTENSIONS = listOf("jpg", "jpeg", "png", "pdf", "gif", "txt")

    /**
     * URL에서 파일 이름 추출
     *
     * @param url 파일 URL
     * @return 파일 이름
     */
    fun extractFileNameFromUrl(url: String?): String {
        if (url == null || !url.contains("/")) {
            throw IllegalArgumentException("유효하지 않은 URL입니다.")
        }
        return url.substring(url.lastIndexOf("/") + 1)
    }

    /**
     * 업로드된 MultipartFile의 확장자가 허용된 목록에 포함되어 있는지 검증
     *
     * @param file 업로드된 파일
     * @throws IllegalArgumentException 허용되지 않는 확장자일 경우 예외 발생
     */
    fun validateFileExtension(file: MultipartFile) {
        val originalFilename = file.originalFilename
            ?: throw IllegalArgumentException("파일 이름에 확장자가 없습니다.")
        
        if (!originalFilename.contains(".")) {
            throw IllegalArgumentException("파일 이름에 확장자가 없습니다.")
        }
        
        val extension = FilenameUtils.getExtension(originalFilename).lowercase()
        if (extension !in ALLOWED_EXTENSIONS) {
            throw IllegalArgumentException("허용되지 않는 파일 확장자입니다. 허용되는 확장자: $ALLOWED_EXTENSIONS")
        }
    }
}
