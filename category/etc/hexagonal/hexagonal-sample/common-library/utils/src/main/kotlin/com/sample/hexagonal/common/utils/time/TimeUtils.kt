package com.sample.hexagonal.common.utils.time

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset

object TimeUtils {

    const val KOREA = "Asia/Seoul"
    const val UTC = "UTC"

    fun convertTimeZone(
        fromTimeZoneId: String,
        toTimeZoneId: String,
        targetLocalDateTime: LocalDateTime,
    ): LocalDateTime {
        val fromZonedDateTime = targetLocalDateTime.atZone(ZoneId.of(fromTimeZoneId))
        val toZonedDateTime = fromZonedDateTime.withZoneSameInstant(ZoneId.of(toTimeZoneId))
        return toZonedDateTime.toLocalDateTime()
    }

    fun LocalDateTime.convertToLocal(toTimeZoneId: String) =
        convertTimeZone(ZoneOffset.UTC.id, toTimeZoneId, this)
}
