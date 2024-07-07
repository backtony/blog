package com.example.authorization.config.security

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "oauth-client")
data class OAuthClientProperties (
    val issuerUrl : String,
    val accessTokenTtl: Duration,
    val refreshTokenTtl: Duration,
    val client: OAuthClient,
)

data class OAuthClient(
    val id: String,
    val clientId: String,
    val clientSecret: String,
    val redirectUri: String,
)
