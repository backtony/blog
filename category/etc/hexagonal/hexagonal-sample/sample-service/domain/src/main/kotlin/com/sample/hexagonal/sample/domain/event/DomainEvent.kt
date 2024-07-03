package com.sample.hexagonal.sample.domain.event

import com.sample.hexagonal.sample.domain.event.AggregateRoot
import java.time.LocalDateTime

interface DomainEvent {
    val key: String
    val aggregateRoot: AggregateRoot
    val registeredDate: LocalDateTime
}
