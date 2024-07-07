package com.example.authorization.config.preset

import com.example.authorization.domain.user.User
import com.example.authorization.repository.user.UserRepository
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

@Component
class Preset {

    @Bean
    fun setAdmin(userRepository: UserRepository, passwordEncoder: PasswordEncoder): ApplicationRunner {
        return ApplicationRunner {
            userRepository.save(
                User.createOperator(
                    loginId = "admin",
                    name = "admin",
                    email = "admin@test.com",
                    password = passwordEncoder.encode("admin"),
                )
            )
        }
    }
}
