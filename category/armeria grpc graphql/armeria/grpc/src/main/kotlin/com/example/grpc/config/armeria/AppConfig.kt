package com.example.grpc.config.armeria

import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.reactive.TransactionalEventPublisher

@Configuration
class AppConfig {

    // https://github.com/spring-projects/spring-framework/issues/27515#issuecomment-1660934318
    @Bean
    fun transactionalEventPublisher(
        applicationEventPublisher: ApplicationEventPublisher,
    ): TransactionalEventPublisher {
        return TransactionalEventPublisher(applicationEventPublisher)
    }
}
