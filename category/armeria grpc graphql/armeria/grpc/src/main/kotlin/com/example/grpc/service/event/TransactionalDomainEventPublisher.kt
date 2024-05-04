package com.example.grpc.service.event

import com.example.event.DomainEvent
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component
import org.springframework.transaction.reactive.TransactionalEventPublisher

@Component
class TransactionalDomainEventPublisher(
    private val transactionalEventPublisher: TransactionalEventPublisher,
) {

    // https://github.com/spring-projects/spring-framework/issues/27515#issuecomment-1660934318
    suspend fun publishEvent(domainEvent: DomainEvent) {
        DomainEventKafkaMessageMapper.mapToKafkaMessage(domainEvent)
            .let { transactionalEventPublisher.publishEvent(it) }
            .awaitSingleOrNull()
    }
}
