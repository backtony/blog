package com.sample.hexagonal.sample.server.batch.config

import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.binder.MeterBinder
import org.springframework.batch.core.explore.JobExplorer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MicrometerMetricConfig {

    @Bean
    fun runningJobMetrics(jobExplorer: JobExplorer): MeterBinder {
        return MeterBinder { registry ->
            Gauge.builder("sample.running.job.count", jobExplorer) { jobExplorer ->
                var runningJobCount = 0
                for (jobName in jobExplorer.jobNames) {
                    runningJobCount += jobExplorer.findRunningJobExecutions(jobName).size
                }
                runningJobCount.toDouble()
            }.register(registry)
        }
    }
}
