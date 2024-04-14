package com.example.grpc.utils

import com.google.protobuf.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId

fun LocalDateTime.toTimestamp(): Timestamp {
    return toTimestamp(ZoneId.systemDefault())
}

fun LocalDateTime.toTimestamp(zoneId: ZoneId): Timestamp {
    val instant = this.atZone(zoneId).toInstant()
    return Timestamp.newBuilder()
        .setNanos(instant.nano)
        .setSeconds(instant.epochSecond)
        .build()
}
