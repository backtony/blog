package com.example.mongo.repository

import com.example.mongo.document.PipelineDocument
import org.bson.types.ObjectId
import org.springframework.data.domain.Pageable
import org.springframework.data.mongodb.repository.MongoRepository

interface PipelineDocumentRepository : MongoRepository<PipelineDocument, ObjectId>, PipelineDocumentCustomRepository {
    fun findByStepsTypeIn(stepsTypes: List<String>): List<PipelineDocument>
}

interface PipelineDocumentCustomRepository {
    fun findByStepStatusIn(stepsStatuses: List<String>): List<PipelineDocument>
    fun countByStepStatusIn(stepsStatuses: List<String>): Long
    fun findPageByStepStatusIn(stepsStatuses: List<String>, pageable: Pageable): List<PipelineDocument>
}
