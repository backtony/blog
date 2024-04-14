package com.example.grpc.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.domain.AuditorAware
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories
import java.time.LocalDateTime
import java.util.*

@Configuration
// https://docs.spring.io/spring-data/r2dbc/docs/current-SNAPSHOT/reference/html/#r2dbc.connecting
@EnableR2dbcRepositories
// https://docs.spring.io/spring-data/relational/reference/r2dbc/auditing.html
@EnableR2dbcAuditing
class R2dbcConfiguration() {

    @Bean
    fun auditorProvider(): AuditorAware<LocalDateTime> {
        return AuditorAware { Optional.of(LocalDateTime.now()) }
    }
}
