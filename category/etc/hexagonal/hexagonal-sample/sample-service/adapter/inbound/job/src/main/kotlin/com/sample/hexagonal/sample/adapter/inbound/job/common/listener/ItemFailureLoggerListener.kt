package com.sample.hexagonal.sample.adapter.inbound.job.common.listener

import mu.KotlinLogging
import org.springframework.batch.core.annotation.OnProcessError
import org.springframework.batch.core.annotation.OnReadError
import org.springframework.batch.core.annotation.OnWriteError
import org.springframework.batch.item.Chunk

class ItemFailureLoggerListener<I, O> {

    private val logger = KotlinLogging.logger {}

    @OnReadError
    fun onReadError(e: Exception) {
        logger.error("occurred error on read. msg : ${e.message}")
    }

    @OnProcessError
    fun onProcessError(item: I & Any, e: Exception) {
        logger.error("occurred error on process items. item : $item, msg : ${e.message}")
    }

    @OnWriteError
    fun onWriteError(e: Exception, items: Chunk<out O>) {
        logger.error("occurred error on write items. msg : ${e.message}")
    }
}
