package com.example.circuit.config

import com.example.circuit.circuit.CircuitBreaker
import com.example.circuit.circuit.CircuitBreakerProvider
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CircuitBreakerConfig {

    @Bean
    fun circuitBreakerProvider(
        circuitBreaker: CircuitBreaker,
    ) = CircuitBreakerProvider(circuitBreaker)
}
