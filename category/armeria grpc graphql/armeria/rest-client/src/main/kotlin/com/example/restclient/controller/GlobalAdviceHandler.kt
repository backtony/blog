package com.example.restclient.controller

import com.example.restclient.utils.TraceSupportUtil
import io.grpc.StatusException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalAdviceHandler(
    private val traceSupportUtil: TraceSupportUtil,
) {

    private val log = KotlinLogging.logger { }

    @ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(value = [StatusException::class])
    fun handleException(e: StatusException): ResponseEntity<String> {
        val traceId = traceSupportUtil.getTraceId()
        val message = "[TraceId : $traceId] msg : ${e.message}"
        log.error(message, e)

        return ResponseEntity.internalServerError().body(e.message)
    }
}
