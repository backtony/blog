package com.example.authorization.service.user

import com.example.authorization.controller.user.dto.CreateUserRequest
import com.example.authorization.domain.user.User
import com.example.authorization.repository.user.UserRepository
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    fun createUser(request: CreateUserRequest): User {

        val user = with(request) {
            User.createOperator(
                loginId = loginId,
                name = name,
                email = email,
                password = passwordEncoder.encode(password),
            )
        }

        return userRepository.save(user)
    }
}
