package com.example.consumer.config.zipkin

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "zipkin")
data class ZipkinProperties(
    val endpoint: String,
    val messageTimeout: Long,
)
