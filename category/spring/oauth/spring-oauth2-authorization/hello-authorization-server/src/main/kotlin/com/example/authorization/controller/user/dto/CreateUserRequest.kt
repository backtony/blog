package com.example.authorization.controller.user.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank

data class CreateUserRequest(
    @field:NotBlank
    val loginId: String,
    @field:NotBlank
    val name: String,
    @field:Email
    val email: String,
    @field:NotBlank
    val password: String,
)
