package com.example.grpc.service.event

import com.example.event.DomainEvent
import com.example.event.InternalTopic
import com.linecorp.armeria.common.RequestContext
import com.linecorp.armeria.server.ServiceRequestContext

object DomainEventKafkaMessageMapper {

    fun mapToKafkaMessage(domainEvent: DomainEvent, current: ServiceRequestContext = RequestContext.current()): KafkaMessage {
        return KafkaMessage(
            topic = InternalTopic.from(domainEvent),
            key = domainEvent.key,
            data = domainEvent,
            type = domainEvent.javaClass.name,
            registeredDate = domainEvent.registeredDate,
            requestContext = current
        )
    }
}
