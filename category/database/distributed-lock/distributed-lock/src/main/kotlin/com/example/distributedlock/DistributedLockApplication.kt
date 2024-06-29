package com.example.distributedlock

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class DistributedLockApplication

fun main(args: Array<String>) {
    runApplication<DistributedLockApplication>(*args)
}
