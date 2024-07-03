package com.sample.hexagonal.sample.server.batch.service

import com.google.common.base.Stopwatch
import com.sample.hexagonal.sample.server.batch.exception.BatchException
import mu.KotlinLogging
import org.springframework.batch.core.JobExecution
import org.springframework.batch.core.JobParameters
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.configuration.JobRegistry
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.TimeUnit

@Service
class JobExecuteService(
    private val jobLauncher: JobLauncher,
    private val jobRegistry: JobRegistry,
    private val jobExplorer: JobExplorer,
) {

    private val logger = KotlinLogging.logger { }

    fun run(jobName: String, params: Map<String, String>): JobExecution {
        val jobParameters = generateJobParameters(params, true)

        try {
            val job = jobRegistry.getJob(jobName)

            val sw = Stopwatch.createStarted()
            val jobExecution = jobLauncher.run(job, jobParameters)
            sw.stop()

            logger.info(
                "$jobName Job exitStatus: ${jobExecution.exitStatus.exitCode}, " +
                    "jobParameters : $jobParameters, elapsed Time: ${sw.elapsed(TimeUnit.MILLISECONDS)}ms.",
            )

            return jobExecution
        } catch (e: Exception) {
            throw BatchException(jobName, jobParameters, e)
        }
    }

    private fun generateJobParameters(params: Map<String, String>, useIncrementer: Boolean = true): JobParameters {
        val builder = JobParametersBuilder()

        if (useIncrementer) {
            builder.addString("UUID", UUID.randomUUID().toString())
        }

        for ((key, value) in params) {
            builder.addString(key, value)
        }

        return builder.toJobParameters()
    }

    fun isAnyJobRunning(): Boolean {
        for (jobName in jobExplorer.jobNames) {
            val runningJobExecutions = jobExplorer.findRunningJobExecutions(jobName)
            if (runningJobExecutions.isNotEmpty()) {
                return true
            }
        }
        return false
    }
}
