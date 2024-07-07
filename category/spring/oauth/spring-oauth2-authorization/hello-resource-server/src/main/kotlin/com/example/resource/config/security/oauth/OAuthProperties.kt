package com.example.resource.config.security.oauth

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth2")
data class OAuthProperties(
    val cookieName: CookieName,
) {

    data class CookieName(
        val accessCookieName: String,
    )
}
