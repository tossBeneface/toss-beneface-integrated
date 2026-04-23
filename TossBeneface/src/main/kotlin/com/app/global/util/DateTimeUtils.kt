package com.app.global.util

import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

object DateTimeUtils {

    // Date -> LocalDateTime
    fun convertToLocalDateTime(date: Date): LocalDateTime {
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
    }

    // LocalDateTime -> Date
    fun convertToDate(localDateTime: LocalDateTime?): Date? {
        if (localDateTime == null) {
            return null
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant())
    }
}
