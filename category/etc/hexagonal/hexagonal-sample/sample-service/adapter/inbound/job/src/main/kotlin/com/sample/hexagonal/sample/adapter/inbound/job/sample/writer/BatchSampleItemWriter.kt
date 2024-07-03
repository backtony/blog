package com.sample.hexagonal.sample.adapter.inbound.job.sample.writer

import mu.KotlinLogging
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter

open class BatchSampleItemWriter : ItemWriter<String> {

    private val logger = KotlinLogging.logger { }
    override fun write(chunk: Chunk<out String>) {
        logger.info { "batchSampleItemWriter write something..." }
        Thread.sleep(1000)
    }
}
