package com.sample.hexagonal.sample.infrastructure.mongo.sample

import com.sample.hexagonal.sample.infrastructure.mongo.sample.SampleDocument.Companion.DOCUMENT_NAME
import org.bson.types.ObjectId
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.mapping.FieldType
import java.time.LocalDateTime

@Document(DOCUMENT_NAME)
data class SampleDocument(
    @Id
    @Field(name = ID, targetType = FieldType.OBJECT_ID)
    val id: ObjectId? = null,

    @Field(NAME)
    val name: String,

    @CreatedDate
    @Field(CREATED_AT)
    val createdAt: LocalDateTime,

    @LastModifiedDate
    @Field(UPDATED_AT)
    var updatedAt: LocalDateTime,
) {

    companion object {
        const val DOCUMENT_NAME = "sampleDocument"
        const val ID = "_id"
        const val NAME = "name"
        const val CREATED_AT = "createdAt"
        const val UPDATED_AT = "updatedAt"
    }
}
