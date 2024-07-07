package com.example.authorization.repository.user

import com.example.authorization.domain.user.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {

    fun findByLoginId(loginId: String): User?
}
