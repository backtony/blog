package com.example.mongo.controller

import com.example.mongo.controller.dto.SaveRequest
import com.example.mongo.domain.Pipeline
import com.example.mongo.service.PipelineService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class PipelineController(
    private val pipelineService: PipelineService,
) {

    @PostMapping("/pipelines")
    fun save(@RequestBody saveRequest: SaveRequest): Pipeline {
        return pipelineService.save(saveRequest)
    }

    @GetMapping("/pipelines/steps/types")
    fun findByStepTypes(@RequestParam types: List<String>): List<Pipeline> {
        return pipelineService.findByStepTypeIn(types)
    }

    @GetMapping("/pipelines/steps/statuses")
    fun findByStepStatuses(@RequestParam statuses: List<String>): List<Pipeline> {
        return pipelineService.findByStepStatusIn(statuses)
    }

    @GetMapping("/pipelines/steps/statuses/count")
    fun countByStepStatuses(@RequestParam statuses: List<String>): Long {
        return pipelineService.countByStepStatusIn(statuses)
    }

    @GetMapping("/pipelines/steps/statuses/page")
    fun findPageByStepStatusIn(@RequestParam statuses: List<String>): List<Pipeline> {
        return pipelineService.findPageByStepStatusIn(statuses)
    }
}
