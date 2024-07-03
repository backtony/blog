package com.sample.hexagonal.common.producer.publisher

import com.sample.hexagonal.common.producer.KafkaMessage
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class TransactionalMessageKafkaPublisher(
    private val commonKafkaTemplate: KafkaTemplate<String, Any>,
) {

    /**
     * commonKafkaTemplate는 CommonKafkaListener에서 공통 에러 처리
     */
    @TransactionalEventListener(value = [KafkaMessage::class], phase = TransactionPhase.AFTER_COMMIT)
    fun sendMessage(message: KafkaMessage) {
        commonKafkaTemplate.send(message.buildProducerRecord())
    }
}
