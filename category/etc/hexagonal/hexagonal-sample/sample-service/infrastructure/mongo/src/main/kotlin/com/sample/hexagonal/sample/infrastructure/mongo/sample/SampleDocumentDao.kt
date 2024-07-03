package com.sample.hexagonal.sample.infrastructure.mongo.sample

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface SampleDocumentDao : MongoRepository<SampleDocument, ObjectId>
