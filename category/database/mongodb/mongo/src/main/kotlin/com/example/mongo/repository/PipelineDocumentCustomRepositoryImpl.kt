package com.example.mongo.repository

import com.example.mongo.document.PipelineDocument
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Repository

@Repository
class PipelineDocumentCustomRepositoryImpl(
    private val mongoTemplate: MongoTemplate,
): PipelineDocumentCustomRepository {

    override fun findByStepStatusIn(stepsStatuses: List<String>): List<PipelineDocument> {
        return mongoTemplate.find(
            PipelineDocumentQueryBuilder.buildQueryToFindByStepStatusIn(stepsStatuses),
            PipelineDocument::class.java
        )
    }

    override fun countByStepStatusIn(stepsStatuses: List<String>): Long {
        return mongoTemplate.count(
            PipelineDocumentQueryBuilder.buildQueryToFindByStepStatusIn(stepsStatuses),
            PipelineDocument::class.java
        )
    }

    override fun findPageByStepStatusIn(stepsStatuses: List<String>, pageable: Pageable): List<PipelineDocument> {
        return mongoTemplate.find(
            PipelineDocumentQueryBuilder.buildQueryToFindPageByStepStatusIn(stepsStatuses, pageable),
            PipelineDocument::class.java
        )
    }
}
