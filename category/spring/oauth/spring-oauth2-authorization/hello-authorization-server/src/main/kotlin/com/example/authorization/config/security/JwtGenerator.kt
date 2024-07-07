package com.example.authorization.config.security

import com.example.authorization.repository.user.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames
import org.springframework.security.oauth2.core.oidc.endpoint.OidcParameterNames
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm
import org.springframework.security.oauth2.jwt.JwsHeader
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimsSet
import org.springframework.security.oauth2.jwt.JwtEncoder
import org.springframework.security.oauth2.jwt.JwtEncoderParameters
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType
import org.springframework.security.oauth2.server.authorization.settings.OAuth2TokenFormat
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator
import java.time.Instant
import java.util.*

class JwtGenerator(
    private val jwtEncoder: JwtEncoder,
    private val userRepository: UserRepository,
) : OAuth2TokenGenerator<Jwt> {

    override fun generate(context: OAuth2TokenContext): Jwt? {
        if (!isValidContext(context)) {
            return null
        }

        val authorization = context.authorization!!
        val loginId = authorization.principalName
        val user = userRepository.findByLoginId(loginId)
            ?: throw RuntimeException("User not found by loginId. $loginId")

        val issuedAt = Instant.now()
        val claimsBuilder = JwtClaimsSet.builder()
            .id(UUID.randomUUID().toString())
            .issuer(context.authorizationServerContext.issuer)
            .audience(listOf(user.id.toString()))
            .claim("userType", user.type.name)
            .claim("userRole", user.role.name)
            .issuedAt(issuedAt)
            .notBefore(issuedAt)
            .expiresAt(issuedAt.plus(context.registeredClient.tokenSettings.accessTokenTimeToLive))


        if (context.authorizedScopes.isNullOrEmpty().not()) {
            claimsBuilder.claim(OAuth2ParameterNames.SCOPE, context.authorizedScopes)
        }

        return jwtEncoder.encode(
            JwtEncoderParameters.from(
                JwsHeader.with(SignatureAlgorithm.RS256).build(),
                claimsBuilder.build(),
            ),
        )
    }

    /**
     * @see org.springframework.security.oauth2.server.authorization.token.JwtGenerator
     */
    private fun isValidContext(context: OAuth2TokenContext): Boolean {
        val tokenType = context.tokenType ?: return false
        val isAccessToken = tokenType == OAuth2TokenType.ACCESS_TOKEN
        val isIdToken = tokenType.value == OidcParameterNames.ID_TOKEN

        if (!isAccessToken && !isIdToken) {
            return false
        }
        if (isAccessToken && context.registeredClient.tokenSettings.accessTokenFormat != OAuth2TokenFormat.SELF_CONTAINED) {
            return false
        }

        return true
    }
}
