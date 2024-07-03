package com.sample.hexagonal.common.producer.handler

import com.sample.hexagonal.common.producer.KafkaMessage

interface MessagePersistenceHandler {
    fun handlePersistence(message: KafkaMessage)
}
