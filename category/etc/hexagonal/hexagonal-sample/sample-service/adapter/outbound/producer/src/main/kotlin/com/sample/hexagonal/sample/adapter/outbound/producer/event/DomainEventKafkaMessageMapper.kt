package com.sample.hexagonal.sample.adapter.outbound.producer.event

import com.sample.hexagonal.common.producer.KafkaMessage
import com.sample.hexagonal.sample.domain.event.DomainEvent
import com.sample.hexagonal.sample.application.support.message.InternalTopic

object DomainEventKafkaMessageMapper {

    fun mapToKafkaMessage(domainEvent: DomainEvent): KafkaMessage {
        return KafkaMessage(
            topic = InternalTopic.from(domainEvent),
            key = domainEvent.key,
            data = domainEvent,
            type = domainEvent.javaClass.name,
            registeredDate = domainEvent.registeredDate,
        )
    }
}
