package com.sample.hexagonal.sample.adapter.inbound.controller.sample.dto.sample

import com.sample.hexagonal.sample.domain.sample.Sample
import java.time.LocalDateTime

data class SampleResponse(
    val id: String,
    val name: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime?,
) {

    companion object {
        fun from(sample: Sample): SampleResponse {
            return with(sample) {
                SampleResponse(
                    id = id!!,
                    name = name,
                    createdAt = createdAt,
                    updatedAt = updatedAt,
                )
            }
        }
    }
}
