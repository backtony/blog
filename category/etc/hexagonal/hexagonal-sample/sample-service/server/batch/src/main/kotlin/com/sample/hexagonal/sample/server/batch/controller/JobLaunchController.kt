package com.sample.hexagonal.sample.server.batch.controller

import com.sample.hexagonal.sample.server.batch.interceptor.RefererCheck
import com.sample.hexagonal.sample.server.batch.service.JobExecuteService
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.JobExecution
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.io.PrintWriter
import java.io.StringWriter

@RestController
@RequestMapping("/jobs")
class JobLaunchController(
    private val jobExecuteService: JobExecuteService,
) {

    @RefererCheck
    @GetMapping("/{jobName}/run")
    fun runJob(
        @PathVariable("jobName") jobName: String,
        @RequestParam params: Map<String, String>,
    ): ResponseEntity<String> {
        val jobExecution = jobExecuteService.run(jobName, params)

        if (jobExecution.exitStatus.exitCode != ExitStatus.COMPLETED.exitCode) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(getAllExceptionsAsString(jobExecution))
        }

        return ResponseEntity.ok().build()
    }

    private fun getAllExceptionsAsString(jobExecution: JobExecution): String {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        jobExecution.allFailureExceptions.forEach { exception ->
            exception.printStackTrace(pw)
        }

        return sw.toString()
    }

    @GetMapping("/running")
    fun isAnyJobRunning(): ResponseEntity<Boolean> {
        return ResponseEntity.ok(jobExecuteService.isAnyJobRunning())
    }
}
