package com.sample.hexagonal.sample.adapter.outbound.repository.sample

import com.sample.hexagonal.sample.domain.sample.Sample
import com.sample.hexagonal.sample.infrastructure.h2.sample.SampleEntity

object SampleMapper {

    fun mapDomainToEntity(sample: Sample): SampleEntity {
        return with(sample) {
            SampleEntity(
                id = id?.toLong(),
                name = name,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }
    }

    fun mapEntityToDomain(entity: SampleEntity): Sample {
        return with(entity) {
            Sample(
                id = id.toString(),
                name = name,
                createdAt = createdAt,
                updatedAt = updatedAt,
            )
        }
    }
}
