package com.example.springgraphql.config.graphql.strategy

import graphql.ExecutionResult
import graphql.ExecutionResultImpl
import graphql.execution.AsyncSerialExecutionStrategy
import graphql.execution.ExecutionContext
import graphql.execution.ExecutionStrategyParameters
import graphql.schema.GraphQLList
import java.util.concurrent.CompletableFuture

class AsyncSerialExecutionStrategy: AsyncSerialExecutionStrategy() {

    override fun completeValueForNull(
        executionContext: ExecutionContext,
        parameters: ExecutionStrategyParameters
    ): CompletableFuture<ExecutionResult> {
        when (parameters.executionStepInfo.unwrappedNonNullType) {
            // List type
            is GraphQLList -> {
                return CompletableFuture.completedFuture(
                    ExecutionResultImpl(emptyList<Any>(), executionContext.errors),
                )
            }
        }

        return super.completeValueForNull(executionContext, parameters)
    }
}
