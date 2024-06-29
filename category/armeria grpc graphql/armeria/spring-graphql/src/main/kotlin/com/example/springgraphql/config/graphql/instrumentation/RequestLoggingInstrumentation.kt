package com.example.springgraphql.config.graphql.instrumentation

import brave.http.HttpTracing
import com.fasterxml.jackson.databind.ObjectMapper
import graphql.ExecutionResult
import graphql.execution.instrumentation.InstrumentationContext
import graphql.execution.instrumentation.InstrumentationState
import graphql.execution.instrumentation.SimplePerformantInstrumentation
import graphql.execution.instrumentation.parameters.InstrumentationExecutionParameters
import mu.KotlinLogging

class RequestLoggingInstrumentation(
    private val httpTracing: HttpTracing,
    private val activeProfile: String,
    private val objectMapper: ObjectMapper,
) : SimplePerformantInstrumentation() {

    private val log = KotlinLogging.logger { }

    override fun beginExecution(
        parameters: InstrumentationExecutionParameters,
        state: InstrumentationState?,
    ): InstrumentationContext<ExecutionResult>? {
        loggingTracingTag(parameters)
        return super.beginExecution(parameters, state)
    }

    private fun loggingTracingTag(
        parameters: InstrumentationExecutionParameters,
    ) {
        try {
            val span = httpTracing
                .tracing()
                .tracer()
                .currentSpan() ?: return

            span.tag("profile", activeProfile)

            if (!parameters.query.isNullOrEmpty()) {
                span.tag("query.string", parameters.query)
            }

            if (!parameters.operation.isNullOrEmpty()) {
                span.tag("query.operationName", parameters.operation)
            }

            /**
             * 모든 이메일 / 아이디 / 비밀번호 필드를 zipkin의 tag로 남지 않도록 하기 위해 주석 처리. 개인정보만 masking 처리할 수 있도록 개선 가능.
             * if (parameters.variables != null && parameters.variables.isNotEmpty()) {
             *     span.tag("query.variables", mapper.writeValueAsString(parameters.variables))
             * }
             */
        } catch (e: Exception) {
            log.warn("loggingTracingTag error : ${objectMapper.writeValueAsString(parameters.executionInput)}")
        }
    }
}
