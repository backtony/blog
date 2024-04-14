package com.example.restclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class RestClientApplication

fun main(args: Array<String>) {
    runApplication<RestClientApplication>(*args)
}
