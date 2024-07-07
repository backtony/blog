package com.example.login

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ClientLoginApplication

fun main(args: Array<String>) {
    runApplication<ClientLoginApplication>(*args)
}
