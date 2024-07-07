package com.example.authorization

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class AuthorizationApplication

fun main(args: Array<String>) {
    runApplication<AuthorizationApplication>(*args)
}
