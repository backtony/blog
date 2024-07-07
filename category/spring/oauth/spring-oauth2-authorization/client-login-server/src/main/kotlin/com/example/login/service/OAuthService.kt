package com.example.login.service

import com.example.login.config.OAuthProperties
import com.example.login.config.OAuthProperties.OAuthClient
import com.example.login.service.dto.TokenResponse
import mu.KotlinLogging
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import org.springframework.web.client.RestTemplate
import java.util.*

@Service
class OAuthService(
    private val oAuthProperties: OAuthProperties,
    private val restTemplate: RestTemplate,
) {

    private val log = KotlinLogging.logger { }

    fun getOAuthLoginUrl(type: String): String {
        return generateOAuthLoginUrl(getOAuthClient(type))
    }

    private fun generateOAuthLoginUrl(client: OAuthClient): String {
        return StringBuilder(client.baseUrl)
            .append("/oauth2/authorize")
            .append("?response_type=code")
            .append("&client_id=${client.clientId}")
            .append("&state=${UUID.randomUUID()}")
            .append("&scope=RESOURCE")
            .toString()
    }

    fun issueToken(type: String, code: String, state: String?): TokenResponse {
        val client = getOAuthClient(type)
        val request = generateIssueTokenRequest(client, code, state)
        val baseUrl = "${client.baseUrl}/oauth2/token"

        return callOrThrow(baseUrl, request)
    }

    private fun getOAuthClient(type: String): OAuthClient {
        return oAuthProperties.getClient(type) ?: throw IllegalArgumentException("$type Type client not found.")
    }

    private fun generateIssueTokenRequest(client: OAuthClient, code: String, state: String?): HttpEntity<MultiValueMap<String, String>> {
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        state?.let { params[STATE] = state }
        params[CODE] = code
        params[GRANT_TYPE] = AUTHORIZATION_CODE
        addClientInfo(params, client)

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
        return HttpEntity(params, headers)
    }

    fun reissueToken(refreshToken: String, type: String): TokenResponse {
        val client = getOAuthClient(type)
        val request = generateReissueTokenRequest(refreshToken, client)
        val baseUrl = "${client.baseUrl}/oauth2/token"

        return callOrThrow<TokenResponse>(baseUrl, request)
    }

    private fun generateReissueTokenRequest(refreshToken: String, client: OAuthClient): HttpEntity<MultiValueMap<String, String>> {
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        addClientInfo(params, client)
        params[GRANT_TYPE] = REFRESH_TOKEN
        params[REFRESH_TOKEN] = refreshToken

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
        return HttpEntity(params, headers)
    }

    fun revokeToken(
        accessToken: String?,
        refreshToken: String?,
        type: String,
    ) {
        if (accessToken.isNullOrBlank() && refreshToken.isNullOrBlank()) {
            throw IllegalArgumentException("There should be at least one token")
        }

        val client = getOAuthClient(type)
        val baseUrl = "${client.baseUrl}/oauth2/revoke"
        if (accessToken.isNullOrBlank().not()) {
            val request = generateRevokeTokenRequest(accessToken!!, ACCESS_TOKEN, client)
            callOrNotThrow(baseUrl, request)
        }

        if (refreshToken.isNullOrBlank().not()) {
            val request = generateRevokeTokenRequest(refreshToken!!, REFRESH_TOKEN, client)
            callOrNotThrow(baseUrl, request)
        }
    }

    private fun generateRevokeTokenRequest(token: String, hint: String, client: OAuthClient): HttpEntity<MultiValueMap<String, String>> {
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        addTokenAndHint(params, token, hint)
        addClientInfo(params, client)

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
        return HttpEntity(params, headers)
    }

    fun introspect(
        accessToken: String?,
        refreshToken: String?,
        type: String,
    ): Map<String, Any?> {
        if (accessToken.isNullOrBlank() && refreshToken.isNullOrBlank()) {
            throw IllegalArgumentException("There should be at least one token")
        }

        val resultMap = mutableMapOf<String, Any?>()
        val client = getOAuthClient(type)
        val baseUrl = "${client.baseUrl}/oauth2/introspect"
        if (accessToken.isNullOrBlank().not()) {
            val request = generateIntrospectRequest(accessToken!!, ACCESS_TOKEN, client)
            resultMap["accessToken"] = callOrThrow(baseUrl, request)
        }

        if (refreshToken.isNullOrBlank().not()) {
            val request = generateIntrospectRequest(refreshToken!!, REFRESH_TOKEN, client)
            resultMap["refreshToken"] = callOrThrow(baseUrl, request)
        }

        return resultMap
    }

    private fun generateIntrospectRequest(token: String, hint: String, client: OAuthClient): HttpEntity<MultiValueMap<String, String>> {
        val params: MultiValueMap<String, String> = LinkedMultiValueMap()
        addTokenAndHint(params, token, hint)
        addClientInfo(params, client)

        val headers = HttpHeaders().apply { contentType = MediaType.APPLICATION_FORM_URLENCODED }
        return HttpEntity(params, headers)
    }

    private fun addClientInfo(
        params: MultiValueMap<String, String>,
        client: OAuthClient,
    ) {
        params[CLIENT_ID] = client.clientId
        params[CLIENT_SECRET] = client.clientSecret
    }

    private fun addTokenAndHint(
        params: MultiValueMap<String, String>,
        token: String,
        hint: String,
    ) {
        params[TOKEN] = token
        params[TOKEN_TYPE_HINT] = hint
    }

    private inline fun <reified T> callOrThrow(
        baseUrl: String,
        request: HttpEntity<MultiValueMap<String, String>>,
    ): T {

        return restTemplate.exchange(
            baseUrl,
            HttpMethod.POST,
            request,
            T::class.java,
        ).body!!
    }

    private fun callOrNotThrow(
        baseUrl: String,
        request: HttpEntity<MultiValueMap<String, String>>,
    ) {
        runCatching {
            restTemplate.exchange(
                baseUrl,
                HttpMethod.POST,
                request,
                Unit::class.java,
            ).body
        }.getOrElse {
            log.error(it.message, it)
        }
    }

    companion object {
        private const val CODE = "code"
        private const val STATE = "state"
        private const val GRANT_TYPE = "grant_type"
        private const val AUTHORIZATION_CODE = "authorization_code"
        private const val CLIENT_ID = "client_id"
        private const val CLIENT_SECRET = "client_secret"
        private const val REFRESH_TOKEN = "refresh_token"
        private const val ACCESS_TOKEN = "access_token"
        private const val TOKEN_TYPE_HINT = "token_type_hint"
        private const val TOKEN = "token"
    }
}
