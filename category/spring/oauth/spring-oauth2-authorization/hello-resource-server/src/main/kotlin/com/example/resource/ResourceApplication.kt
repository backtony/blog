package com.example.resource

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ResourceApplication

fun main(args: Array<String>) {
    runApplication<ResourceApplication>(*args)
}
