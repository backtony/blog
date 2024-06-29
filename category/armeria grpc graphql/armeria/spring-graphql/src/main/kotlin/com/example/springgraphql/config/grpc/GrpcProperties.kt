package com.example.springgraphql.config.grpc

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "grpc")
data class GrpcProperties(
    val endpoint: String,
    val port: Int,
    val timeout: Long,
)
