package com.example.restclient.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "zipkin")
data class ZipkinProperties(
    val endpoint: String,
    val messageTimeout: Long,
)
