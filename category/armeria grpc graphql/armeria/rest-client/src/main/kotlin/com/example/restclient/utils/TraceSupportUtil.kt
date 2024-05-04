package com.example.restclient.utils

import brave.Tracer
import org.springframework.stereotype.Component

@Component
class TraceSupportUtil(
    private val tracer: Tracer,
) {

    fun getTraceId(): String {
        return tracer.currentSpan().context().traceIdString()
    }
}
