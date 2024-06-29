package com.example.springgraphql.config.graphql

import brave.http.HttpTracing
import com.example.springgraphql.config.graphql.instrumentation.RequestLoggingInstrumentation
import com.example.springgraphql.config.graphql.strategy.AsyncExecutionStrategy
import com.example.springgraphql.config.graphql.strategy.AsyncSerialExecutionStrategy
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.benmanes.caffeine.cache.Caffeine
import com.tailrocks.graphql.datetime.LocalDateScalar
import com.tailrocks.graphql.datetime.LocalDateTimeScalar
import graphql.analysis.MaxQueryComplexityInstrumentation
import graphql.analysis.MaxQueryDepthInstrumentation
import graphql.execution.instrumentation.ChainedInstrumentation
import graphql.execution.preparsed.PreparsedDocumentEntry
import graphql.execution.preparsed.PreparsedDocumentProvider
import graphql.scalars.ExtendedScalars
import graphql.validation.rules.OnValidationErrorStrategy
import graphql.validation.rules.ValidationRules
import graphql.validation.schemawiring.ValidationSchemaWiring
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.graphql.GraphQlSourceBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.graphql.execution.RuntimeWiringConfigurer
import java.util.concurrent.TimeUnit
import java.util.function.Function


@Configuration
class GraphQLConfig {

    @Value("\${spring.profiles.active}")
    lateinit var activeProfile: String

    private val log = KotlinLogging.logger { }

    /**
     *
     * config
     * https://docs.spring.io/spring-graphql/reference/request-execution.html#execution.graphqlsource.runtimewiring-configurer
     *
     * scalar
     * https://github.com/graphql-java/graphql-java-extended-scalars?tab=readme-ov-file#spring-for-graphql
     * https://github.com/graphql-java/graphql-java-extended-scalars?tab=readme-ov-file#java-primitives
     *
     * directive extend validation
     * https://github.com/graphql-java/graphql-java-extended-validation?tab=readme-ov-file#schema-directive-wiring
     */
    @Bean
    fun runtimeWiringConfigurer(): RuntimeWiringConfigurer {

        val validationRules = ValidationRules.newValidationRules()
            .onValidationErrorStrategy(OnValidationErrorStrategy.RETURN_NULL)
            .build()

        return RuntimeWiringConfigurer { builder ->
            builder
                .scalar(ExtendedScalars.GraphQLLong)
                .scalar(ExtendedScalars.Json)
                .scalar(LocalDateTimeScalar.create(null, true, null))
                .scalar(LocalDateScalar.create(null, true, null))
                .directiveWiring(ValidationSchemaWiring(validationRules))
        }
    }

    @Bean
    fun graphQLSourceBuilderCustomizer(
        httpTracing: HttpTracing,
        objectMapper: ObjectMapper,
    ): GraphQlSourceBuilderCustomizer {
        // https://docs.spring.io/spring-graphql/reference/request-execution.html#execution.graphqlsource.operation-caching
        // https://www.graphql-java.com/documentation/execution/#query-caching
        val cache = Caffeine.newBuilder()
            .recordStats()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .initialCapacity(500)
            .maximumSize(1_000)
            .build<String, PreparsedDocumentEntry>()

        val provider = PreparsedDocumentProvider { executionInput, computeFunction ->
            val mapCompute = Function<String, PreparsedDocumentEntry> { key -> computeFunction.apply(executionInput) }
            cache[executionInput.query, mapCompute]
        }

        return GraphQlSourceBuilderCustomizer { sourceBuilder ->
            sourceBuilder.configureGraphQl { builder ->
                builder.queryExecutionStrategy(AsyncExecutionStrategy())
                builder.mutationExecutionStrategy(AsyncSerialExecutionStrategy())
                builder.preparsedDocumentProvider(provider)
                builder.instrumentation(
                    ChainedInstrumentation(
                        RequestLoggingInstrumentation(httpTracing, activeProfile, objectMapper),
                        MaxQueryComplexityInstrumentation(100), // 1필드당 복잡도 1
                        MaxQueryDepthInstrumentation(20), // 20뎁스까지 가능
                    ),
                )
            }
        }
    }
}

// 복잡도는 17이고 3뎁스에 해당한다.
//query teams($id: Long!) {
//    member(id: $id) {
//        id
//        name
//        introduction
//        type
//        country
//        team {
//            id
//            name
//            registeredBy
//            registeredDate
//            modifiedBy
//            modifiedDate
//        }
//        registeredBy
//        registeredDate
//        modifiedBy
//        modifiedDate
//    }
//}
