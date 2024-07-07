package com.example.authorization.config.security

import com.example.authorization.repository.user.UserRepository
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException

class CustomUserDetailsService(
    private val userRepository: UserRepository,
) : UserDetailsService {

    override fun loadUserByUsername(username: String): UserDetails {

        val user = userRepository.findByLoginId(username)
            ?: throw UsernameNotFoundException(username)

        return User.builder()
            .username(user.loginId)
            .password(user.password)
            .authorities(SimpleGrantedAuthority("ROLE_".plus(user.role.name)))
            .build()
    }
}
