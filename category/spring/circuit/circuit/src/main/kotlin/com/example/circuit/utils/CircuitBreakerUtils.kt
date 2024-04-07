package com.example.circuit.utils

import com.example.circuit.circuit.CircuitBreaker
import com.example.circuit.circuit.CircuitBreakerProvider
import com.example.circuit.exception.CircuitOpenException
import io.github.resilience4j.circuitbreaker.CallNotPermittedException

/**
 * @param name 서킷 브레이커의 이름으로, 서킷 브레이커 인스턴스를 구별하는 데 사용
 *             기본값("default")
 * @param circuitBreaker 실행할 함수를 보호할 서킷 브레이커 인스턴스
 *             기본값("공통 싱글턴 인스턴스")
 * @param f 실행할 함수. 이 함수는 서킷 브레이커의 하위에서 실행
 */
fun <T> circuit(
    name: String = "default",
    circuitBreaker: CircuitBreaker = CircuitBreakerProvider.get(),
    f: () -> T,
): Result<T> = circuitBreaker.run(name, f)

fun <T> Result<T>.fallback(f: (e: Throwable?) -> T): Result<T> = when (this.isSuccess) {
    true -> this
    false -> runCatching { f(this.exceptionOrNull()) }
}

fun <T> Result<T>.fallbackIfOpen(f: (e: Throwable?) -> T): Result<T> = when (this.exceptionOrNull()) {
    is CircuitOpenException -> runCatching { f(this.exceptionOrNull()) }
    else -> this
}

fun Throwable.convertToCustomException(): Throwable = when (this) {
    is CallNotPermittedException -> CircuitOpenException()
    else -> this
}
