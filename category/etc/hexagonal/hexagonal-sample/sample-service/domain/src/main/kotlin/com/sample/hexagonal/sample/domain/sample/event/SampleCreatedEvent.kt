package com.sample.hexagonal.sample.domain.sample.event

import com.sample.hexagonal.sample.domain.event.AggregateRoot
import com.sample.hexagonal.sample.domain.event.DomainEvent
import java.time.LocalDateTime

class SampleCreatedEvent(
    val sampleId: String,
    override val registeredDate: LocalDateTime = LocalDateTime.now()
) : DomainEvent {
    override val key = sampleId
    override val aggregateRoot = AggregateRoot.SAMPLE
}
