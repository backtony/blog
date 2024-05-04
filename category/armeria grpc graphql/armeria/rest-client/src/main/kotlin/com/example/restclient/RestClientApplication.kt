package com.example.restclient

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import reactor.core.publisher.Hooks

@ConfigurationPropertiesScan
@SpringBootApplication
class RestClientApplication

fun main(args: Array<String>) {
    runApplication<RestClientApplication>(*args)
    // https://github.com/spring-projects/spring-boot/issues/33372#issuecomment-1443766925
    // zipkin
    Hooks.enableAutomaticContextPropagation()
}
