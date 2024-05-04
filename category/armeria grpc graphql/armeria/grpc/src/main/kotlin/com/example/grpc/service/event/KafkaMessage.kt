package com.example.grpc.service.event

import com.linecorp.armeria.common.RequestContext
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.LocalDateTime

data class KafkaMessage(
    var id: String? = null,
    val topic: String,
    val key: String,
    val type: String,
    val data: Any,
    var published: Boolean = false,
    val registeredDate: LocalDateTime,
    val headers: MutableMap<String, String> = mutableMapOf(),
    val requestContext: RequestContext,
) {
    fun buildProducerRecord(): ProducerRecord<String, Any> {
        return ProducerRecord(topic, key, data).apply {
            headers.entries.forEach {
                this.headers().add(it.key, it.value.toByteArray())
            }
        }
    }
}
