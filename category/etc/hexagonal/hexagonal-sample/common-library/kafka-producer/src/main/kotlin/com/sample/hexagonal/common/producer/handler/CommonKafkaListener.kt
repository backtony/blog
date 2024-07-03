package com.sample.hexagonal.common.producer.handler

import mu.KotlinLogging
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.clients.producer.RecordMetadata
import org.springframework.kafka.support.ProducerListener

class CommonKafkaListener : ProducerListener<String, Any> {

    private val log = KotlinLogging.logger { }

    override fun onError(producerRecord: ProducerRecord<String, Any>, recordMetadata: RecordMetadata?, exception: java.lang.Exception?) {
        log.error(
            "Fail to send kafka Message. Topic: ${producerRecord.topic()}, Partition: ${producerRecord.partition()}," +
                " Key: ${producerRecord.key()},  Value: ${producerRecord.value()}",
            exception,
        )
    }
}
