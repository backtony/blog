package com.sample.hexagonal.sample.server.batch.config

import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration
import org.springframework.batch.support.transaction.ResourcelessTransactionManager
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

/**
 * https://docs.spring.io/spring-batch/reference/job/java-config.html#page-title
 */
@Configuration
class BatchConfig : DefaultBatchConfiguration() {

    override fun getDataSource(): DataSource {
        return EmbeddedDatabaseBuilder()
            .setType(EmbeddedDatabaseType.H2)
            .addScript("/org/springframework/batch/core/schema-drop-h2.sql")
            .addScript("/org/springframework/batch/core/schema-h2.sql")
            .build()
    }

    override fun getTransactionManager(): PlatformTransactionManager {
        return ResourcelessTransactionManager()
    }
}
