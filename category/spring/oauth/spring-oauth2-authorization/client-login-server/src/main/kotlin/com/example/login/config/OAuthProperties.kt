package com.example.login.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "oauth2")
data class OAuthProperties(
    val clients: List<OAuthClient>,
    val cookieName: CookieName,
    val cookieDomain: String,
) {

    fun getClient(type: String): OAuthClient? {
        return clients.find { it.type.name == type.uppercase() }
    }

    data class OAuthClient(
        val type: Type,
        val baseUrl: String,
        val clientId: String,
        val clientSecret: String,
    )

    data class CookieName(
        val accessCookieName: String,
        val refreshCookieName: String,
        val loginTypeCookieName: String,
    )

    enum class Type {
        HELLO
    }
}
