package com.sample.hexagonal.common.producer

import org.apache.kafka.clients.producer.ProducerRecord
import java.time.LocalDateTime

data class KafkaMessage(
    var id: String? = null,
    val topic: String,
    val key: String,
    val type: String,
    val data: Any,
    var published: Boolean = false, // https://blog.gangnamunni.com/post/transactional-outbox/
    val registeredDate: LocalDateTime,
    val headers: MutableMap<String, String> = mutableMapOf(),
) {
    fun buildProducerRecord(): ProducerRecord<String, Any> {
        return ProducerRecord(topic, key, data).apply {
            headers.entries.forEach {
                this.headers().add(it.key, it.value.toByteArray())
            }
        }
    }
}
