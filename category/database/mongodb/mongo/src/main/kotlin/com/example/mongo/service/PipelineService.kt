package com.example.mongo.service

import com.example.mongo.controller.dto.SaveRequest
import com.example.mongo.document.PipelineDocument
import com.example.mongo.domain.Pipeline
import com.example.mongo.repository.PipelineDocumentRepository
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class PipelineService(
    private val pipelineRepository: PipelineDocumentRepository,
) {

    @Transactional
    fun save(request: SaveRequest): Pipeline {

        val pipeline = Pipeline(
            steps = listOf(
                Pipeline.Step(
                    request.type
                )
            ),
        )

        return pipelineRepository.save(PipelineDocument.from(pipeline)).toDomain()
    }

    fun findByStepTypeIn(stepTypes: List<String>): List<Pipeline> {
        return pipelineRepository.findByStepsTypeIn(stepTypes)
            .map { it.toDomain() }
    }

    fun findByStepStatusIn(stepStatuses: List<String>): List<Pipeline> {
        return pipelineRepository.findByStepStatusIn(stepStatuses)
            .map { it.toDomain() }
    }

    fun countByStepStatusIn(stepStatuses: List<String>): Long {
        return pipelineRepository.countByStepStatusIn(stepStatuses)
    }

    fun findPageByStepStatusIn(stepStatuses: List<String>): List<Pipeline> {
        val pageable = PageRequest.of(0, 10)
        return pipelineRepository.findPageByStepStatusIn(stepStatuses, pageable)
            .map { it.toDomain() }
    }
}
