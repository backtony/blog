package com.sample.hexagonal.sample.adapter.inbound.controller.sample

import com.sample.hexagonal.sample.adapter.inbound.controller.sample.dto.sample.SampleResponse
import com.sample.hexagonal.sample.adapter.inbound.controller.sample.dto.sample.SampleSaveRequest
import com.sample.hexagonal.sample.adapter.inbound.controller.sample.dto.sample.SampleUpdateRequest
import com.sample.hexagonal.sample.application.port.inbound.sample.SampleDeleteInboundPort
import com.sample.hexagonal.sample.application.port.inbound.sample.SampleFindInboundPort
import com.sample.hexagonal.sample.application.port.inbound.sample.SampleSaveInboundPort
import org.springframework.web.bind.annotation.*

@RestController
class SampleController(
    private val sampleFindInboundPort: SampleFindInboundPort,
    private val sampleSaveInboundPort: SampleSaveInboundPort,
    private val sampleDeleteInboundPort: SampleDeleteInboundPort,
) {

    @GetMapping("/v1/sample/{id}")
    fun findSample(@PathVariable id: String): SampleResponse {
        val sample = sampleFindInboundPort.getSample(id)
        return SampleResponse.from(sample)
    }

    @PostMapping("/v1/sample")
    fun saveSample(@RequestBody sampleSaveRequest: SampleSaveRequest): SampleResponse {
        val sample = sampleSaveInboundPort.saveSample(sampleSaveRequest.name)
        return SampleResponse.from(sample)
    }

    @PatchMapping("/v1/sample/{id}")
    fun updateSample(@PathVariable id: String, @RequestBody sampleUpdateRequest: SampleUpdateRequest): SampleResponse {
        val sample = sampleSaveInboundPort.updateSample(id, sampleUpdateRequest.name)
        return SampleResponse.from(sample)
    }

    @DeleteMapping("/v1/sample/{id}")
    fun deleteSample(@PathVariable id: String): String {
        return sampleDeleteInboundPort.deleteSample(id)
    }
}
