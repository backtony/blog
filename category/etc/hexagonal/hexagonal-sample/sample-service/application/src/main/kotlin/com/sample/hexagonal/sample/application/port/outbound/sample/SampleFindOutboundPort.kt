package com.sample.hexagonal.sample.application.port.outbound.sample

import com.sample.hexagonal.sample.domain.sample.Sample

interface SampleFindOutboundPort {

    fun findById(id: String): Sample?
}
