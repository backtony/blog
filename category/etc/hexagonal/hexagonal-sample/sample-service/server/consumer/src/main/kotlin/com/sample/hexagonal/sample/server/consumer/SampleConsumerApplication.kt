package com.sample.hexagonal.sample.server.consumer

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.time.ZoneOffset
import java.util.*

@SpringBootApplication(
    scanBasePackages = [
        "com.sample.hexagonal.sample.server.consumer",
        "com.sample.hexagonal.sample.application",
        "com.sample.hexagonal.sample.adapter.inbound",
        "com.sample.hexagonal.sample.adapter.outbound",
        "com.sample.hexagonal.sample.infrastructure",
        "com.sample.hexagonal.common"
    ],
)
class SampleConsumerApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))
    runApplication<SampleConsumerApplication>(*args)
}
