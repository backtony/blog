package com.example.event.member

import com.example.event.AggregateRoot
import com.example.event.DomainEvent
import java.time.LocalDateTime

data class MemberCreatedEvent(
    val memberId: Long,
    override val registeredDate: LocalDateTime = LocalDateTime.now()
) : DomainEvent {
    override val key = memberId.toString()
    override val aggregateRoot = AggregateRoot.MEMBER
}
