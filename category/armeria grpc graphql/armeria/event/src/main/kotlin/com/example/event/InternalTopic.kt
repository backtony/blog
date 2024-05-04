package com.example.event

object InternalTopic {
    const val MEMBER = "MEMBER"
    const val TEAM = "TEAM"

    fun from(domainEvent: DomainEvent): String {
        return when (domainEvent.aggregateRoot) {
            AggregateRoot.MEMBER -> MEMBER
            AggregateRoot.TEAM -> TEAM
        }
    }
}
