package com.app.payment.application.dto

data class TossApiResponse(
    val statusCode: Int,
    val body: Map<String, Any> = emptyMap()
) {
    private val normalizedBody: Map<String, Any> = LinkedHashMap(body)

    fun isSuccess(): Boolean {
        return statusCode in 200..299 && !normalizedBody.containsKey("error")
    }

    fun getRequiredString(key: String): String {
        return normalizedBody[key]?.toString()
            ?: throw IllegalArgumentException("Missing Toss response field: $key")
    }

    fun getRequiredInt(key: String): Int {
        return when (val value = normalizedBody[key]) {
            is Number -> value.toInt()
            is String -> value.toInt()
            else -> throw IllegalArgumentException("Missing Toss response field: $key")
        }
    }

    fun getOptionalLong(key: String): Long? {
        val value = normalizedBody[key] ?: return null
        return when (value) {
            is Number -> value.toLong()
            is String -> if (value.isNotBlank()) value.toLong() else null
            else -> null
        }
    }
}
