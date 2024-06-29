package com.example.springgraphql.controller.advice

import graphql.GraphQLError
import graphql.GraphqlErrorBuilder
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.data.method.annotation.GraphQlExceptionHandler
import org.springframework.graphql.execution.ErrorType
import org.springframework.web.bind.annotation.ControllerAdvice

@ControllerAdvice
class GlobalExceptionHandler {

    @GraphQlExceptionHandler
    fun handleRuntimeException(ex: RuntimeException, env: DataFetchingEnvironment): GraphQLError {
        return GraphqlErrorBuilder.newError()
            .message(ex.message)
            .path(env.executionStepInfo.path)
            .location(env.field.sourceLocation)
            .errorType(ErrorType.INTERNAL_ERROR)
            .build()
    }
}
