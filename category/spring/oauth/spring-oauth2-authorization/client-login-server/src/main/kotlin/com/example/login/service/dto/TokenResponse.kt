package com.example.login.service.dto

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
data class TokenResponse(
    val accessToken: String,
    val refreshToken: String?,
    val scope: String?,
    val tokenType: String,
    val expiresIn: Long,
)
