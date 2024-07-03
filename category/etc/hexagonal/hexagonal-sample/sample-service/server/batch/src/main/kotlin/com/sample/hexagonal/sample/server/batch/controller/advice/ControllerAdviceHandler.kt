package com.sample.hexagonal.sample.server.batch.controller.advice

import com.sample.hexagonal.sample.server.batch.exception.BatchException
import com.sample.hexagonal.sample.server.batch.exception.InvalidRefererException
import jakarta.servlet.http.HttpServletRequest
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class ControllerAdviceHandler {

    private val logger = KotlinLogging.logger {}

    @ExceptionHandler(value = [BatchException::class])
    fun handleBatchException(e: BatchException, request: HttpServletRequest): ResponseEntity<String> {
        logger.error("Job Failed : jobName: ${e.jobName}, jobParameters : ${e.jobParameters}, msg: ${e.message}", e)

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.message)
    }

    @ExceptionHandler(value = [InvalidRefererException::class])
    fun handleInvalidRefererException(
        ex: InvalidRefererException,
        request: HttpServletRequest,
    ): ResponseEntity<String> {
        logger.error("Job executed without referer.")

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }
}
