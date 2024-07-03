package com.sample.hexagonal.sample.infrastructure.mongo.config

import com.mongodb.ConnectionString
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.data.mongodb.core.convert.DbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.MongoMappingContext
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

/**
 * https://www.baeldung.com/spring-data-mongodb-transactions
 * https://docs.spring.io/spring-data/mongodb/docs/current-SNAPSHOT/reference/html/#mongo.transactions.tx-manager
 */
// 트랜잭션 옵션을 사용하려면 mongodb가 replica set 형태로 구성되어야한다.
@Configuration
@EnableMongoAuditing
@EnableMongoRepositories(
    basePackages = [
        "com.sample.hexagonal.sample.infrastructure.mongo",
    ],
)
class MongoClientConfig(
    @Value("\${spring.data.mongodb.database}") private val database: String,
    @Value("\${spring.data.mongodb.uri}") private val uri: String,
 ) : AbstractMongoClientConfiguration() {

    @Bean("mongoTransactionManager")
    fun mongoTransactionManager(dbFactory: MongoDatabaseFactory): MongoTransactionManager {
        return MongoTransactionManager(dbFactory)
    }

    override fun getDatabaseName(): String {
        return database
    }

    override fun mongoClient(): MongoClient {
        return MongoClients.create(
            MongoClientSettings.builder()
                .applyConnectionString(ConnectionString(uri))
                .build(),
        )
    }

    override fun mappingMongoConverter(
        databaseFactory: MongoDatabaseFactory,
        customConversions: MongoCustomConversions,
        mappingContext: MongoMappingContext,
    ): MappingMongoConverter {
        super.mappingMongoConverter(databaseFactory, customConversions, mappingContext)
        val dbRefResolver: DbRefResolver = DefaultDbRefResolver(databaseFactory)
        val mappingConverter = MappingMongoConverter(dbRefResolver, mappingContext)

        mappingConverter.customConversions = customConversions

        // db에 _class 컬럼을 남기지 않는 설정
        mappingConverter.setTypeMapper(DefaultMongoTypeMapper(null))

        return mappingConverter
    }
 }
