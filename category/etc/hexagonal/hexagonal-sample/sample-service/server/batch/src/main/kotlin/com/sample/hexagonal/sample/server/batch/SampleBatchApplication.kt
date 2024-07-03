package com.sample.hexagonal.sample.server.batch

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.time.ZoneOffset
import java.util.*

@SpringBootApplication(
    scanBasePackages = [
        "com.sample.hexagonal.sample.server.batch",
        "com.sample.hexagonal.sample.application",
        "com.sample.hexagonal.sample.adapter",
        "com.sample.hexagonal.sample.infrastructure",
        "com.sample.hexagonal.common"
    ],
)
class SampleBatchApplication

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone(ZoneOffset.UTC))
    runApplication<SampleBatchApplication>(*args)
}
