package com.example.mongo.repository

import com.example.mongo.document.PipelineDocument
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query

object PipelineDocumentQueryBuilder {

    fun buildQueryToFindByStepStatusIn(stepStatuses: List<String>): Query {
        return Query().addCriteria(
            Criteria.where("${PipelineDocument.STEPS}.${PipelineDocument.STATUS}").`in`(stepStatuses)
        )
    }

    fun buildQueryToFindPageByStepStatusIn(stepStatuses: List<String>, pageable: Pageable): Query {
        return Query().addCriteria(
            Criteria.where("${PipelineDocument.STEPS}.${PipelineDocument.STATUS}").`in`(stepStatuses)
        )
            .with(pageable)
            .with(Sort.by(Sort.Direction.DESC, "$PipelineDocument.ID"))
    }
}
