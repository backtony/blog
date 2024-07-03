package com.sample.hexagonal.sample.application.port.inbound.sample

import com.sample.hexagonal.sample.domain.sample.Sample

interface SampleSaveInboundPort {
    fun saveSample(name: String): Sample

    fun updateSample(id: String, name: String): Sample
}
