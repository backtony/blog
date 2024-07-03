package com.sample.hexagonal.sample.server.batch.exception

import org.springframework.batch.core.JobParameters

class BatchException(
    val jobName: String,
    val jobParameters: JobParameters,
    ex: Throwable,
) : RuntimeException(ex.message, ex)
