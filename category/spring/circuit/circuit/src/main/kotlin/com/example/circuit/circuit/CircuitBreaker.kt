package com.example.circuit.circuit

import com.example.circuit.utils.convertToCustomException
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory
import org.springframework.stereotype.Component

interface CircuitBreaker {
    fun <T> run(name: String, block: () -> T): Result<T>
}

@Component
class DefaultCircuitBreaker(
    private val factory: CircuitBreakerFactory<*, *>,
) : CircuitBreaker {

    override fun <T> run(name: String, block: () -> T): Result<T> = runCatching {
        factory.create(name).run(block) { e -> throw e.convertToCustomException() }
    }
}
