package com.example.resource.config.security.oauth

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletRequestWrapper
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.WebUtils

/**
 * @see org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
 * 실제 검증은 BearerTokenAuthenticationFilter에서 처리
 */
class OAuth2CookieTokenFilter(
    private val oAuthProperties: OAuthProperties,
) : OncePerRequestFilter() {

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {

        val accessToken = WebUtils.getCookie(request, oAuthProperties.cookieName.accessCookieName)?.value
        if (accessToken.isNullOrBlank()) {
            return filterChain.doFilter(request, response)
        }

        val wrappedRequest: HttpServletRequestWrapper = object : HttpServletRequestWrapper(request) {
            override fun getHeader(name: String): String? {
                return if (name == HttpHeaders.AUTHORIZATION) {
                    "$BEARER $accessToken"
                } else {
                    super.getHeader(name)
                }
            }
        }
        return filterChain.doFilter(wrappedRequest, response)
    }

    companion object {
        private const val BEARER = "Bearer"
    }
}
