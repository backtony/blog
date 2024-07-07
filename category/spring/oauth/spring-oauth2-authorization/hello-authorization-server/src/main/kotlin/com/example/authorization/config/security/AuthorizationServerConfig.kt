package com.example.authorization.config.security

import com.example.authorization.repository.user.UserRepository
import com.nimbusds.jose.jwk.JWKSet
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.source.ImmutableJWKSet
import com.nimbusds.jose.jwk.source.JWKSource
import com.nimbusds.jose.proc.SecurityContext
import org.apache.tomcat.util.codec.binary.Base64
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.MediaType
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.oauth2.core.AuthorizationGrantType
import org.springframework.security.oauth2.core.ClientAuthenticationMethod
import org.springframework.security.oauth2.jwt.JwtDecoder
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationConsentService
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService
import org.springframework.security.oauth2.server.authorization.client.JdbcRegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings
import org.springframework.security.oauth2.server.authorization.token.DelegatingOAuth2TokenGenerator
import org.springframework.security.oauth2.server.authorization.token.OAuth2AccessTokenGenerator
import org.springframework.security.oauth2.server.authorization.token.OAuth2RefreshTokenGenerator
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher
import org.springframework.security.web.util.matcher.RequestMatcher
import java.security.KeyFactory
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant


/**
 * https://docs.spring.io/spring-authorization-server/reference/getting-started.html
 */
@Configuration
@EnableWebSecurity // (debug = true)
class AuthorizationServerConfig(
    @Value("\${management.server.port}") private val managementPort: Int,
    private val oAuthClientProperties: OAuthClientProperties,
    private val jwkSourceProperties: JWKSourceProperties,
    private val jdbcTemplate: JdbcTemplate,
) {

    // ignore security to actuator port
    @Bean
    fun webSecurityCustomizer(): WebSecurityCustomizer {
        return WebSecurityCustomizer { web ->
            web.ignoring().requestMatchers(RequestMatcher { it.localPort == managementPort })
        }
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    fun authorizationServerSecurityFilterChain(
        http: HttpSecurity,
    ): SecurityFilterChain {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http)
        http.getConfigurer(OAuth2AuthorizationServerConfigurer::class.java)
//            .oidc(Customizer.withDefaults()) // Enable OpenID Connect 1.0

        http
            // Redirect to the login page when not authenticated from the
            // authorization endpoint
            .exceptionHandling { exceptions ->
                exceptions
                    .defaultAuthenticationEntryPointFor(
                        LoginUrlAuthenticationEntryPoint("/login"),
                        MediaTypeRequestMatcher(MediaType.TEXT_HTML),
                    )
            } // Accept access tokens for User Info and/or Client Registration
            .oauth2ResourceServer { resourceServer ->
                resourceServer
                    .jwt(Customizer.withDefaults())
            }

        return http.build()
    }

    @Bean
    @Order(2)
    @Throws(Exception::class)
    fun defaultSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            // rest api csrf disable
            .csrf { csrf: CsrfConfigurer<HttpSecurity> ->
                csrf.ignoringRequestMatchers("/v1/**")
            }
            .authorizeHttpRequests { authorize ->
                authorize
                    .requestMatchers("/v1/**").permitAll()
                    .anyRequest().authenticated()
            }
            .formLogin(Customizer.withDefaults())
//            // only allow /oauth2 path to loginPage
            .exceptionHandling { exceptions ->
                exceptions
                    .defaultAuthenticationEntryPointFor(
                        JsonUnAuthorizedErrorEntryPoint(),
                    ) { request -> request.servletPath.startsWith("/oauth2").not() }
            }

        return http.build()
    }

    @Bean
    fun oAuth2AuthorizationService(): OAuth2AuthorizationService {
        return JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository())
    }

    @Bean
    fun oAuth2AuthorizationConsentService(): OAuth2AuthorizationConsentService {
        return JdbcOAuth2AuthorizationConsentService(jdbcTemplate, registeredClientRepository())
    }

    @Bean
    fun registeredClientRepository(): RegisteredClientRepository {
        val client = getRegisteredClientCodeAndRefreshType(
            oAuthClientProperties.client,
            setOf(Scope.RESOURCE),
        )

        return JdbcRegisteredClientRepository(jdbcTemplate).also { repository ->
            saveRegisteredClient(repository, client)
        }
    }

    private fun getRegisteredClientCodeAndRefreshType(
        oAuthClient: OAuthClient,
        scopes: Set<Scope>,
    ): RegisteredClient {
        return baseRegisteredClientBuilder(oAuthClient, scopes)
            .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
            .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
            .redirectUri(oAuthClient.redirectUri)
            .build()
    }

    private fun baseRegisteredClientBuilder(oAuthClient: OAuthClient, scopes: Set<Scope>): RegisteredClient.Builder {
        val clientBuilder = RegisteredClient.withId(oAuthClient.id)
            .clientId(oAuthClient.clientId)
            .clientSecret(passwordEncoder().encode(oAuthClient.clientSecret))
            .clientIdIssuedAt(Instant.now())
            .clientSecretExpiresAt(null)
            // 클라이언트가 토큰을 발급받기 위해 요청을 보낼 때 사용할 수 있는 방식
            .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
            // scope를 추가한 경우, 동의 여부를 물을 것인지 세팅
            .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
            .tokenSettings(
                TokenSettings.builder()
                    .accessTokenTimeToLive(oAuthClientProperties.accessTokenTtl)
                    .refreshTokenTimeToLive(oAuthClientProperties.refreshTokenTtl)
                    .reuseRefreshTokens(false)
                    .build(),
            )

        if (scopes.isNotEmpty()) {
            clientBuilder.scopes {
                it.addAll(scopes.map { it.name })
            }
        }

        return clientBuilder
    }

    private fun saveRegisteredClient(
        repository: JdbcRegisteredClientRepository,
        client: RegisteredClient,
    ) {
        val foundClient = repository.findById(client.id)
        if (foundClient == null || foundClient != client) {
            repository.save(client)
        }
    }

    // oidc의 역할을 하기 위해, userInfo 같은 path로 토큰가지고 요청이 오면 검증해야하기 때문에 필요
    @Bean
    fun jwtDecoder(jwkSource: JWKSource<SecurityContext>): JwtDecoder {
        return OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource)
    }

    // authorizationServer에서는 다른 서버들에게 jwk set = 암호화 정보를 제공해야 한다.
    @Bean
    fun jwkSource(): JWKSource<SecurityContext> {
        val rsaKey = generateRSAKey(jwkSourceProperties)
        val jwkSet = JWKSet(rsaKey)
        jwkSourceProperties.destroy()
        return ImmutableJWKSet(jwkSet)
    }

    // instance를 여러개 띄우게 된다면 만들어진 keyPair을 공유할 수 있도록 yml에 암호화해서 넣어두고 주입받는 형식으로 사용해야 한다.
    private fun generateRSAKey(jwkSourceProperties: JWKSourceProperties): RSAKey {
        val kf = KeyFactory.getInstance(RSA)

        val decodePublicKey = Base64.decodeBase64(String(jwkSourceProperties.publicKey))
        val x509EncodedKeySpec = X509EncodedKeySpec(decodePublicKey)
        val publicKey = kf.generatePublic(x509EncodedKeySpec) as RSAPublicKey

        val decodePrivateKey = Base64.decodeBase64(String(jwkSourceProperties.privateKey))
        val pkcS8EncodedKeySpec = PKCS8EncodedKeySpec(decodePrivateKey)
        val privateKey = kf.generatePrivate(pkcS8EncodedKeySpec) as RSAPrivateKey

        return RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(jwkSourceProperties.kid)
            .build()
    }

    @Bean
    fun tokenGenerator(userRepository: UserRepository): OAuth2TokenGenerator<*> {
        return DelegatingOAuth2TokenGenerator(
            JwtGenerator(NimbusJwtEncoder(jwkSource()), userRepository),
            OAuth2AccessTokenGenerator(),
            OAuth2RefreshTokenGenerator(),
        )
    }

    @Bean
    fun authorizationServerSettings(): AuthorizationServerSettings {
        return AuthorizationServerSettings.builder().issuer(oAuthClientProperties.issuerUrl).build()
    }


    @Bean
    fun userDetailsService(userRepository: UserRepository): UserDetailsService {
        return CustomUserDetailsService(userRepository)
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    companion object {
        const val RSA = "RSA"
    }
}
