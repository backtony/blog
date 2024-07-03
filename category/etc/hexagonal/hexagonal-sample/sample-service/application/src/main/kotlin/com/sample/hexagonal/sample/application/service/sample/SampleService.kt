package com.sample.hexagonal.sample.application.service.sample

import com.sample.hexagonal.common.exception.getOrThrowNotFound
import com.sample.hexagonal.sample.application.port.inbound.sample.SampleDeleteInboundPort
import com.sample.hexagonal.sample.application.port.inbound.sample.SampleFindInboundPort
import com.sample.hexagonal.sample.application.port.inbound.sample.SampleSaveInboundPort
import com.sample.hexagonal.sample.application.port.outbound.event.DomainEventPublishOutboundPort
import com.sample.hexagonal.sample.application.port.outbound.sample.SampleDeleteOutboundPort
import com.sample.hexagonal.sample.application.port.outbound.sample.SampleFindOutboundPort
import com.sample.hexagonal.sample.application.port.outbound.sample.SampleSaveOutboundPort
import com.sample.hexagonal.sample.domain.sample.Sample
import com.sample.hexagonal.sample.domain.sample.event.SampleCreatedEvent
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SampleService(
    private val sampleDeleteOutboundPort: SampleDeleteOutboundPort,
    private val sampleFindOutboundPort: SampleFindOutboundPort,
    private val sampleSaveOutboundPort: SampleSaveOutboundPort,
    private val domainEventPublishOutboundPort: DomainEventPublishOutboundPort,
) : SampleDeleteInboundPort, SampleSaveInboundPort, SampleFindInboundPort {

    @Transactional
    override fun deleteSample(id: String): String {
        sampleDeleteOutboundPort.deleteById(id)
        return id
    }

    @Transactional(readOnly = true)
    override fun getSample(id: String): Sample {
        return sampleFindOutboundPort.findById(id).getOrThrowNotFound(id)
    }

    @Transactional
    override fun saveSample(name: String): Sample {
        return sampleSaveOutboundPort.save(
            Sample.create(name),
        ).also { domainEventPublishOutboundPort.publishEvent(SampleCreatedEvent(it.id!!)) }
    }

    @Transactional
    override fun updateSample(id: String, name: String): Sample {
        return sampleFindOutboundPort.findById(id)
            .getOrThrowNotFound(id)
            .update(name)
            .let { sampleSaveOutboundPort.save(it) }
    }
}
