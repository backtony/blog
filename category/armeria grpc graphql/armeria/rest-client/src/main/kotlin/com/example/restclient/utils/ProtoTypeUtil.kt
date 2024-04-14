package com.example.restclient.utils

import com.google.protobuf.Timestamp
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

fun Timestamp.toLocalDateTime(): LocalDateTime {
    return Instant.ofEpochSecond(this.seconds, this.nanos.toLong())
        .atZone(ZoneId.systemDefault())
        .toLocalDateTime()
}
