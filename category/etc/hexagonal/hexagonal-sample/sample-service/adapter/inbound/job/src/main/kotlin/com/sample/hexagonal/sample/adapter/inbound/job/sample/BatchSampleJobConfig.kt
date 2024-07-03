package com.sample.hexagonal.sample.adapter.inbound.job.sample

import com.sample.hexagonal.sample.adapter.inbound.job.common.listener.ItemFailureLoggerListener
import com.sample.hexagonal.sample.adapter.inbound.job.sample.reader.BatchSampleItemReader
import com.sample.hexagonal.sample.adapter.inbound.job.sample.writer.BatchSampleItemWriter
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class BatchSampleJobConfig(
    private val jobRepository: JobRepository,
    private val transactionManager: PlatformTransactionManager,
) {

    @Bean
    fun batchSampleJob(): Job {
        return JobBuilder(JOB_NAME, jobRepository)
            .start(batchSampleStep())
            .build()
    }

    @Bean
    fun batchSampleStep(): Step {
        return StepBuilder(STEP_NAME, jobRepository)
            .chunk<String, String>(CHUNK_SIZE, transactionManager)
            .reader(batchSampleItemReader(null))
            .writer(batchSampleItemWriter())
            .listener(ItemFailureLoggerListener<String, String>())
            .build()
    }

    @Bean
    @StepScope
    fun batchSampleItemReader(
        @Value("#{jobParameters[requestDate]}") requestDate: String?,
    ): BatchSampleItemReader {
        return BatchSampleItemReader(
            chunkSize = CHUNK_SIZE,
        )
    }

    @Bean
    @StepScope
    fun batchSampleItemWriter(): BatchSampleItemWriter {
        return BatchSampleItemWriter()
    }

    companion object {
        private const val JOB_NAME = "batchSampleJob"
        private const val STEP_NAME = "batchSampleJobStep"
        private const val CHUNK_SIZE = 100
    }
}
