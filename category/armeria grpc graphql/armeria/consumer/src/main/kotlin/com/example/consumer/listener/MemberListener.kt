package com.example.consumer.listener

import com.example.consumer.config.kafka.ConsumerConfig
import com.example.event.InternalTopic
import com.example.event.member.MemberCreatedEvent
import mu.KotlinLogging
import org.springframework.kafka.annotation.KafkaHandler
import org.springframework.kafka.annotation.KafkaListener
import org.springframework.stereotype.Component

@Component
@KafkaListener(
    groupId = InternalTopic.MEMBER,
    topics = [InternalTopic.MEMBER],
    containerFactory = ConsumerConfig.COMMON,
)
class MemberListener {

    private val log = KotlinLogging.logger { }

    @KafkaHandler
    fun handleEvent(event: MemberCreatedEvent) {
        log.info { "event listen : $event" }
    }
}
