package com.sample.hexagonal.sample.application.port.inbound.sample

interface SampleDeleteInboundPort {
    fun deleteSample(id: String): String
}
