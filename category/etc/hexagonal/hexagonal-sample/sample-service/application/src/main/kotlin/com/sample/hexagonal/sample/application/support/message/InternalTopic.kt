package com.sample.hexagonal.sample.application.support.message

import com.sample.hexagonal.sample.domain.event.AggregateRoot
import com.sample.hexagonal.sample.domain.event.DomainEvent

object InternalTopic {
    const val SAMPLE = "SAMPLE"

    fun from(domainEvent: DomainEvent): String {
        return when (domainEvent.aggregateRoot) {
            AggregateRoot.SAMPLE -> SAMPLE
        }
    }
}
