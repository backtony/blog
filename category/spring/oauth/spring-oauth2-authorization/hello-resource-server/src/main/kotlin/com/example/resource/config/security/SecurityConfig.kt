package com.example.resource.config.security

import com.example.resource.config.security.expression.ResourcePermissionEvaluator
import com.example.resource.config.security.oauth.JWTAuthenticationConvertFilter
import com.example.resource.config.security.oauth.OAuth2CookieTokenFilter
import com.example.resource.config.security.oauth.OAuthProperties
import com.webtoons.wcs.security.permission.WcsSecurityExpressionHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.access.PermissionEvaluator
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html#oauth2resourceserver-jwt-sansboot
 */
@Configuration
@EnableMethodSecurity(securedEnabled = true, prePostEnabled = true)
class SecurityConfig(
    private val oAuthProperties: OAuthProperties,
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http.csrf { csrf ->
            csrf.disable()
        }
            .authorizeHttpRequests { authorize ->
                authorize
                    .anyRequest().hasAuthority("SCOPE_RESOURCE")
            }
            .oauth2ResourceServer { resourceServer ->
                resourceServer.jwt(Customizer.withDefaults())
            }


        http.addFilterBefore(OAuth2CookieTokenFilter(oAuthProperties), UsernamePasswordAuthenticationFilter::class.java)
        http.addFilterAfter(
            JWTAuthenticationConvertFilter(),
            BearerTokenAuthenticationFilter::class.java,
        )
        return http.build()
    }

    @Bean
    fun permissionEvaluator(): PermissionEvaluator {
        return ResourcePermissionEvaluator()
    }

    companion object {

        // https://docs.spring.io/spring-security/reference/servlet/authorization/method-security.html#customizing-expression-handling
        @Bean
        fun methodSecurityExpressionHandler(permissionEvaluator: PermissionEvaluator): MethodSecurityExpressionHandler {
            return WcsSecurityExpressionHandler().apply {
                setPermissionEvaluator(permissionEvaluator)
            }
        }
    }
}
