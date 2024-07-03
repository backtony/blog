package com.sample.hexagonal.sample.adapter.outbound.producer.event

import com.sample.hexagonal.sample.application.port.outbound.event.DomainEventPublishOutboundPort
import com.sample.hexagonal.sample.domain.event.DomainEvent
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Component

@Component
class TransactionalDomainEventProducer(
    private val applicationEventPublisher: ApplicationEventPublisher,
) : DomainEventPublishOutboundPort {

    override fun publishEvent(domainEvent: DomainEvent) {
        DomainEventKafkaMessageMapper.mapToKafkaMessage(domainEvent)
            .let { applicationEventPublisher.publishEvent(it) }
    }
}
