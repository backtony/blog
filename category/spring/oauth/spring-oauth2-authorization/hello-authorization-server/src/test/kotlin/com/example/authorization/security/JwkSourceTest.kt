package com.example.authorization.security

import org.junit.jupiter.api.Test
import java.security.KeyPairGenerator
import java.util.Base64

class JwkSourceTest {

    @Test
    fun createJwkSourceKey() {
        val keyPairGenerator = KeyPairGenerator.getInstance("RSA")
        keyPairGenerator.initialize(2048)
        val keyPair = keyPairGenerator.generateKeyPair()

        println(Base64.getEncoder().encodeToString(keyPair.public.encoded))
        println(Base64.getEncoder().encodeToString(keyPair.private.encoded))
    }

}
