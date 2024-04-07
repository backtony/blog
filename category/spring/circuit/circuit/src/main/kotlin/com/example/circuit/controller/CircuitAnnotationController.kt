package com.example.circuit.controller

import com.example.circuit.dto.Article
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CircuitAnnotationController() {

    // fallback은 본 함수와 인자가 일치해야함.
    @CircuitBreaker(name = "article", fallbackMethod = "failSample")
    @GetMapping("/annotation/articles/{id}")
    fun getSampleArticle(): Article {
        val list = listOf(
            IllegalStateException("illegalState"),
            IllegalArgumentException("illegalArgument"),
        )
        throw list.random()
    }

    private fun failSample(throwable: IllegalArgumentException): Article {
        return Article("IllegalArgumentException title", "IllegalArgumentExceptionfail body")
    }

    private fun failSample(e: IllegalStateException): Article {
        return Article("IllegalStateException title", "IllegalStateExceptionfail body")
    }
}
