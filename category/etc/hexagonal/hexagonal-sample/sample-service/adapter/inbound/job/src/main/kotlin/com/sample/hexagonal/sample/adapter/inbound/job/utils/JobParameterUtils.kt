package com.sample.hexagonal.sample.adapter.inbound.job.utils

import mu.KotlinLogging
import org.springframework.util.StringUtils
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

private val logger = KotlinLogging.logger { }

fun String?.convertToLocalDateTime(): LocalDateTime {
    val pattern = "yyyyMMddHHmmss"

    if (!StringUtils.hasText(this)) {
        throw IllegalArgumentException("requestDate should not be null or blank")
    }

    return runCatching {
        LocalDateTime.parse(this, DateTimeFormatter.ofPattern(pattern))
    }.getOrElse { ex ->
        if (ex is DateTimeParseException) {
            logger.error { "requestDate : $this must have the following pattern : $pattern" }
        }
        throw ex
    }
}
