package com.example.authorization.controller.user

import com.example.authorization.controller.user.dto.CreateUserRequest
import com.example.authorization.service.user.UserService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class UserController(
    private val userService: UserService,
) {

    @PostMapping("/v1/users")
    fun createUser(@RequestBody @Valid request: CreateUserRequest): Long {
        return userService.createUser(request).id!!
    }
}
