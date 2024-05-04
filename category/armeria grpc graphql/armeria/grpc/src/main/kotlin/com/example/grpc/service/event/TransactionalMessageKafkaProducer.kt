package com.example.grpc.service.event

import mu.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.event.TransactionPhase
import org.springframework.transaction.event.TransactionalEventListener

@Component
class TransactionalMessageKafkaProducer(
    private val commonKafkaTemplate: KafkaTemplate<String, Any>,
) {

    private val log = KotlinLogging.logger {  }
    /**
     * spring webflux + armeria의 경우 TransactionalEventListener로 이벤트를 수신하면
     * Armeria의 RequestContext가 끊기기 때문에 zipkin을 위해 이어주기 위한 작업이 필요 => ctx.push로 사용 완료 후 close(use) 필요
     * producer 스레드의 warn 로그는 여기를 참고 : https://github.com/line/armeria/issues/2181
     */
    @TransactionalEventListener(value = [KafkaMessage::class], phase = TransactionPhase.AFTER_COMMIT)
    fun sendMessage(message: KafkaMessage) {
        message.requestContext.push().use {
            commonKafkaTemplate.send(message.buildProducerRecord())
        }
    }
}
