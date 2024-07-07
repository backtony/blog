package com.example.resource.config.security.oauth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.web.filter.OncePerRequestFilter

/**
 * @see org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
 * BearerTokenAuthenticationFilter 에서 이미 JWT 토큰을 검증하고 SecurityContext에 JWTAuthentication을 넣으므로 여기서 원하는 Authentication 으로 수정
 */
class JWTAuthenticationConvertFilter : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val authentication = SecurityContextHolder.getContext().authentication
        if (authentication !is JwtAuthenticationToken || authentication.principal !is Jwt) {
            return filterChain.doFilter(request, response)
        }

        val user = generateUser(authentication)
        SecurityContextHolder.getContext().authentication = UsernamePasswordAuthenticationToken(user, null, user.authorities)
            .apply {
                details = WebAuthenticationDetailsSource().buildDetails(request)
            }

        return filterChain.doFilter(request, response)
    }

    private fun generateUser(authentication: JwtAuthenticationToken): User {

        val claims = (authentication.principal as Jwt).claims
        return User(
            id = (claims["aud"] as List<String>).first().toLong(),
            type = claims["userType"].toString(),
            role = claims["userRole"].toString(),
            authorities = authentication.authorities
        )
    }
}
