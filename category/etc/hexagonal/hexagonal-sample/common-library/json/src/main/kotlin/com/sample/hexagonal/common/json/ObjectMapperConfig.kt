package com.sample.hexagonal.common.json

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary

@Configuration
class ObjectMapperConfig {

    @Primary
    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapperFactory.create()
}

