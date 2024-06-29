package com.example.springgraphql.config.graphql.strategy

import graphql.ExecutionResult
import graphql.ExecutionResultImpl
import graphql.execution.AsyncExecutionStrategy
import graphql.execution.ExecutionContext
import graphql.execution.ExecutionStrategyParameters
import graphql.schema.GraphQLList
import java.util.concurrent.CompletableFuture

class AsyncExecutionStrategy: AsyncExecutionStrategy() {

    /**
     * @see graphql.execution.AsyncExecutionStrategy.getFieldValueInfoForNull
     *
     * gql 응답값이 null이면 위 함수가 호출되고 아래 함수를 호출한다.
     * 응답 타입이 list인데 null이 들어온 경우, emptyList로 반환하도록 한다.
     */
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
