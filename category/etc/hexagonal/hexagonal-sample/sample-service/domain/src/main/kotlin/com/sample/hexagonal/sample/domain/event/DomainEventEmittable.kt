package com.sample.hexagonal.sample.domain.event

import com.sample.hexagonal.sample.domain.event.DomainEvent

abstract class DomainEventEmittable {

    val domainEvents: MutableSet<DomainEvent> = mutableSetOf()

    fun registerEvent(event: DomainEvent) {
        domainEvents.add(event)
    }
}
