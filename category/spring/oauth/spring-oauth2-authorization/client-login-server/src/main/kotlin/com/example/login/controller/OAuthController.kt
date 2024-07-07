package com.example.login.controller

import com.example.login.config.OAuthProperties
import com.example.login.service.OAuthService
import com.example.login.service.dto.TokenResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpHeaders.SET_COOKIE
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.view.RedirectView
import org.springframework.web.util.WebUtils

@RequestMapping("/v1/oauth2")
@RestController
class OAuthController(
    private val oAuthService: OAuthService,
    private val oAuthProperties: OAuthProperties,
) {

    @GetMapping("/login/{type}")
    fun login(@PathVariable type: String): RedirectView {
        return RedirectView(oAuthService.getOAuthLoginUrl(type))
    }

    @GetMapping("/{type}/callback/authorization-code")
    fun issueTokenCallback(
        @PathVariable type: String,
        @RequestParam("code") code: String,
        @RequestParam(name = "state", required = false) state: String?,
    ): ResponseEntity<Unit> {
        val token = oAuthService.issueToken(type, code, state)

        return ResponseEntity.ok()
            .headers(generateTokenCookieHeaders(token, type))
            .build()
    }

    @PostMapping("/reissue")
    fun reissueToken(request: HttpServletRequest): ResponseEntity<Unit> {
        val type = getValueFromCookie(request, oAuthProperties.cookieName.loginTypeCookieName)
        val tokenResponse = oAuthService.reissueToken(
            refreshToken = getValueFromCookie(request, oAuthProperties.cookieName.refreshCookieName),
            type = type
        )

        return ResponseEntity.ok()
            .headers(generateTokenCookieHeaders(tokenResponse, type))
            .build()
    }

    private fun getValueFromCookie(request: HttpServletRequest, cookieName: String): String {
        return WebUtils.getCookie(request, cookieName)?.value
            ?: throw IllegalArgumentException("$cookieName not found.")
    }

    private fun generateTokenCookieHeaders(tokenResponse: TokenResponse, type: String): HttpHeaders {
        return HttpHeaders().apply {
            add(SET_COOKIE, generateCookie(oAuthProperties.cookieName.loginTypeCookieName, type).toString())
            add(SET_COOKIE, generateCookie(oAuthProperties.cookieName.accessCookieName, tokenResponse.accessToken).toString())
            tokenResponse.refreshToken?.let {
                add(SET_COOKIE, generateCookie(oAuthProperties.cookieName.refreshCookieName, it).toString())
            }
        }
    }

    private fun generateCookie(cookieName: String, cookieValue: String): ResponseCookie {
        return ResponseCookie.from(cookieName, cookieValue)
            .secure(true)
            .domain(oAuthProperties.cookieDomain)
            .path("/")
            .httpOnly(true)
            .build()
    }

    /**
     * Management API
     */
    @GetMapping("/introspect")
    fun introspect(
        request: HttpServletRequest,
    ): Any {
        return oAuthService.introspect(
            accessToken = getValueFromCookieOrNull(request, oAuthProperties.cookieName.accessCookieName),
            refreshToken = getValueFromCookieOrNull(request, oAuthProperties.cookieName.refreshCookieName),
            type = getValueFromCookie(request, oAuthProperties.cookieName.loginTypeCookieName)
        )
    }

    @PostMapping("/logout")
    fun logout(
        request: HttpServletRequest,
    ): ResponseEntity<Unit> {
        oAuthService.revokeToken(
            accessToken = getValueFromCookieOrNull(request, oAuthProperties.cookieName.accessCookieName),
            refreshToken = getValueFromCookieOrNull(request, oAuthProperties.cookieName.refreshCookieName),
            type = getValueFromCookie(request, oAuthProperties.cookieName.loginTypeCookieName)
        )
        return ResponseEntity.ok()
            .headers(generateLogoutCookieHeaders())
            .build()
    }

    private fun getValueFromCookieOrNull(request: HttpServletRequest, cookieName: String): String? {
        return WebUtils.getCookie(request, cookieName)?.value
    }

    private fun generateLogoutCookieHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            add(
                SET_COOKIE,
                generateLogoutCookie(oAuthProperties.cookieName.accessCookieName).toString(),
            )
            add(
                SET_COOKIE,
                generateLogoutCookie(oAuthProperties.cookieName.refreshCookieName).toString(),
            )
            add(
                SET_COOKIE,
                generateLogoutCookie(oAuthProperties.cookieName.loginTypeCookieName).toString(),
            )
        }
    }

    private fun generateLogoutCookie(cookieName: String): ResponseCookie {
        return ResponseCookie.from(cookieName, "")
            .domain(oAuthProperties.cookieDomain)
            .path("/")
            .httpOnly(true)
            .maxAge(0)
            .build()
    }
}
