package com.example.springgraphql

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import reactor.core.publisher.Hooks

@SpringBootApplication
@ConfigurationPropertiesScan
class SpringGraphqlApplication

fun main(args: Array<String>) {
    runApplication<SpringGraphqlApplication>(*args)
    Hooks.enableAutomaticContextPropagation()
}
