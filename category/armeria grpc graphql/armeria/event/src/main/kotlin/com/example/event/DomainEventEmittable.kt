package com.example.event

abstract class DomainEventEmittable {

    val domainEvents: MutableSet<DomainEvent> = mutableSetOf()

    fun registerEvent(event: DomainEvent) {
        domainEvents.add(event)
    }
}
