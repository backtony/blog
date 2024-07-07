package com.example.authorization.domain.user

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import java.time.LocalDateTime

@Entity
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val loginId: String,
    val name: String,
    type: Type,
    role: Role,
    password: String,
    email: String,
    @CreatedDate
    val registeredDate: LocalDateTime = LocalDateTime.now(),
    @LastModifiedDate
    val modifiedDate: LocalDateTime = LocalDateTime.now(),
) {

    var password: String = password
        private set

    var email: String = email
        private set

    @Enumerated(EnumType.STRING)
    var type: Type = type
        private set

    @Enumerated(EnumType.STRING)
    var role: Role = role
        private set

    enum class Type {
        OPERATOR,
    }

    enum class Role {
        RESOURCE_READ,
        RESOURCE_WRITE,
    }

    companion object {
        fun createOperator(
            loginId: String,
            name: String,
            email: String,
            password: String,
        ): User {

            return User(
                loginId = loginId,
                name = name,
                type = Type.OPERATOR,
                role = Role.RESOURCE_READ,
                password = password,
                email = email,
            )
        }
    }
}
