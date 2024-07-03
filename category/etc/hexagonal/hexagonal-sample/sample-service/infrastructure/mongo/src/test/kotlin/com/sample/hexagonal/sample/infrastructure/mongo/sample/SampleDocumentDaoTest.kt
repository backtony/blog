package com.sample.hexagonal.sample.infrastructure.mongo.sample

import io.kotest.core.spec.style.StringSpec
import org.bson.types.ObjectId
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension

@DataMongoTest
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class SampleDocumentDaoTest(
    private val sampleDocumentDao: SampleDocumentDao,
) : StringSpec(
    {

        /**
         * {"find": "sampleDocument", "filter": {"_id": {"$oid": "6682ce8fca55f2018a251b0a"}}, "limit": 1, "singleBatch": true, "$db": "test"
         */
        "findById" {
            sampleDocumentDao.findById(ObjectId())
        }
    },
)
