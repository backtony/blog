package com.sample.hexagonal.sample.application.port.outbound.event

import com.sample.hexagonal.sample.domain.event.DomainEvent
import com.sample.hexagonal.sample.domain.event.DomainEventEmittable

interface DomainEventPublishOutboundPort {

    fun publishEvent(domainEvent: DomainEvent)

    fun publishEventIfNeeded(domainEventEmittable: DomainEventEmittable) =
        domainEventEmittable.domainEvents.forEach { publishEvent(it) }
}
