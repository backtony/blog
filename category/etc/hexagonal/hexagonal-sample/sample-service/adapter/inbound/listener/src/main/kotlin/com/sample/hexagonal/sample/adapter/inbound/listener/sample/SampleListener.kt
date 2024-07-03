package com.sample.hexagonal.sample.adapter.inbound.listener.sample

import com.sample.hexagonal.common.consumer.KafkaListenerContainFactoryNames
import com.sample.hexagonal.sample.domain.sample.event.SampleCreatedEvent
import com.sample.hexagonal.sample.application.support.message.InternalTopic
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@KafkaListener(
    groupId = InternalTopic.SAMPLE,
    topics = [InternalTopic.SAMPLE],
    containerFactory = KafkaListenerContainFactoryNames.COMMON,
)
class SampleListener {

    private val log = KotlinLogging.logger { }

    @KafkaHandler
    fun handleEvent(event: SampleCreatedEvent) {
        log.info { "Event received : $event" }
    }
}
