package com.sample.hexagonal.sample.adapter.outbound.repository.sample

import com.sample.hexagonal.sample.application.port.outbound.sample.SampleDeleteOutboundPort
import com.sample.hexagonal.sample.application.port.outbound.sample.SampleFindOutboundPort
import com.sample.hexagonal.sample.application.port.outbound.sample.SampleSaveOutboundPort
import com.sample.hexagonal.sample.domain.sample.Sample
import com.sample.hexagonal.sample.infrastructure.h2.sample.SampleEntityDao
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class SampleRepository(
    private val sampleDao: SampleEntityDao,
) : SampleDeleteOutboundPort, SampleFindOutboundPort, SampleSaveOutboundPort {
    override fun deleteById(id: String) {
        sampleDao.deleteById(id.toLong())
    }

    override fun findById(id: String): Sample? {
        return sampleDao.findByIdOrNull(id.toLong())
            ?.let { SampleMapper.mapEntityToDomain(it) }
    }

    override fun save(sample: Sample): Sample {
        return sampleDao.save(SampleMapper.mapDomainToEntity(sample))
            .let { SampleMapper.mapEntityToDomain(it) }
    }
}
