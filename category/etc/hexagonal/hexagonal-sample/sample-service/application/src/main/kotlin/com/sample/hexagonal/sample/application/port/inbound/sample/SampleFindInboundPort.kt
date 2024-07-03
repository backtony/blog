package com.sample.hexagonal.sample.application.port.inbound.sample

import com.sample.hexagonal.sample.domain.sample.Sample

interface SampleFindInboundPort {
    fun getSample(id: String): Sample
}
