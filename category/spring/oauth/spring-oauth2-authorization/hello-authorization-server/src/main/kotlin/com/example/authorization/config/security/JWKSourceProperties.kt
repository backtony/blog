package com.example.authorization.config.security

import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.*

/**
 * publicKey, privateKey 생성법은 JwkSourceTest 참고
 */
@ConfigurationProperties(prefix = "jwk-source")
data class JWKSourceProperties(
    val kid: String,
    val publicKey: CharArray,
    val privateKey: CharArray,
) {
    fun destroy() {
        if (kid != null) {
            Arrays.fill(publicKey, 0.toChar())
            Arrays.fill(privateKey, 0.toChar())
        }
    }
}
