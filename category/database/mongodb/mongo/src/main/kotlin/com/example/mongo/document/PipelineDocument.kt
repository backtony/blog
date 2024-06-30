package com.example.mongo.document

import com.example.mongo.document.PipelineDocument.Companion.DOCUMENT_NAME
import com.example.mongo.domain.Pipeline
import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.time.LocalDateTime

@Document(DOCUMENT_NAME)
class PipelineDocument(
    @Field(ID) @Id val id: ObjectId? = null,
    @Field(STEPS) val steps: List<StepDocument>,
    @Field(STATUS) val status: String,
    @Field(REGISTERED_BY) val registeredBy: String,
    @Field(REGISTERED_DATE) val registeredDate: LocalDateTime,
) {

    fun toDomain(): Pipeline {
        return Pipeline(
            id = id?.toString(),
            steps = steps.map { it.toDomain() },
            status = Pipeline.Status.valueOf(status),
            registeredBy = registeredBy,
            registeredDate = registeredDate,
        )
    }

    @Document(StepDocument.DOCUMENT_NAME)
    data class StepDocument(
        @Field(TYPE) val type: String,
        @Field(STATUS) val status: String,
        @Field(REGISTERED_BY) val registeredBy: String,
        @Field(REGISTERED_DATE) val registeredDate: LocalDateTime,
    ) {

        fun toDomain(): Pipeline.Step {
            return Pipeline.Step(
                type = type,
                status = Pipeline.Step.Status.valueOf(status),
                registeredBy = registeredBy,
                registeredDate = registeredDate,
            )
        }

        companion object {
            fun from(step: Pipeline.Step): StepDocument {
                return with(step) {
                    StepDocument(
                        type = type,
                        status = status.name,
                        registeredBy = registeredBy,
                        registeredDate = registeredDate
                    )
                }
            }

            const val DOCUMENT_NAME = "step"
            const val TYPE = "type"
            const val STATUS = "status"
            const val REGISTERED_BY = "registeredBy"
            const val REGISTERED_DATE = "registeredDate"
        }
    }

    companion object {
        fun from(pipeline: Pipeline): PipelineDocument {
            return with(pipeline) {
                PipelineDocument(
                    id = id?.let { ObjectId(it) },
                    steps = pipeline.steps.map { StepDocument.from(it) },
                    status = status.toString(),
                    registeredBy = registeredBy,
                    registeredDate = registeredDate,
                )
            }
        }

        const val DOCUMENT_NAME = "pipeline"
        const val ID = "_id"
        const val STEPS = "steps"
        const val STATUS = "status"
        const val REGISTERED_BY = "registeredBy"
        const val REGISTERED_DATE = "registeredDate"
    }
}
