package com.example.circuit.controller

import com.example.circuit.dto.Article
import com.example.circuit.utils.circuit
import com.example.circuit.utils.fallback
import com.example.circuit.utils.fallbackIfOpen
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CircuitUtilController() {

    @GetMapping("/util/articles/fallback")
    fun getFallbackSampleArticle(): Article {
        return circuit("fallback-article") {
            throw RuntimeException("runtime")
        }.fallback {
            Article("Fallback title", "Fallback body")
        }.getOrThrow()
    }

    @GetMapping("/util/articles/open")
    fun getFallbackOpenSampleArticle(): Article {
        return circuit("fallback-open-article") {
            throw RuntimeException("runtime")
        }.fallbackIfOpen {
            Article("Fallback Open Default title", "Fallback Open Default body")
        }.getOrThrow()
    }
}
