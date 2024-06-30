package com.example.mongo

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class MongoApplication

fun main(args: Array<String>) {
    runApplication<MongoApplication>(*args)
}
