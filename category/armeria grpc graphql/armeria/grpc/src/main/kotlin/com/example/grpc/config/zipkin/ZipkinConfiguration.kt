package com.example.grpc.config.zipkin

import brave.propagation.CurrentTraceContext
import brave.propagation.CurrentTraceContextCustomizer
import com.linecorp.armeria.common.brave.RequestContextCurrentTraceContext
import mu.KotlinLogging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import zipkin2.Span
import zipkin2.codec.Encoding
import zipkin2.codec.SpanBytesEncoder
import zipkin2.reporter.AsyncReporter
import zipkin2.reporter.Sender
import zipkin2.reporter.kafka.KafkaSender
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * @see org.springframework.boot.actuate.autoconfigure.tracing.BraveAutoConfiguration
 * @see https://armeria.dev/docs/advanced-zipkin/
 * 대부분 autoConfig에서 만들어주기 때문에 transport로 kafka를 사용하기 위한 설정,
 * Armeria 공식문서에 명시된 currentTraceContext를 변경해야하는 부분만 수정
 */
@Configuration
class ZipkinConfiguration(
    private val zipkinProperties: ZipkinProperties,
) {

    private val log = KotlinLogging.logger { }

    @Bean
    fun reporter(sender: Sender): AsyncReporter<Span> {
        val reporter = AsyncReporter.builder(sender).build(SpanBytesEncoder.PROTO3)

        // zipkin에 전송하기 전에 app이 종료되는 경우 방지
        // https://github.com/openzipkin/zipkin-reporter-java/issues/202
        // https://github.com/spring-cloud/spring-cloud-sleuth/blob/3.1.x/spring-cloud-sleuth-autoconfigure/src/main/java/org/springframework/cloud/sleuth/autoconfig/zipkin2/ZipkinAutoConfiguration.java#L136
        Runtime.getRuntime().addShutdownHook(
            Thread {
                log.info { "Flushing remaining spans on shutdown" }
                reporter.flush()
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(zipkinProperties.messageTimeout) + 500)
                    log.debug { "Flushing done - closing the reporter" }
                    reporter.close()
                } catch (e: Exception) {
                    throw IllegalStateException(e)
                }
            },
        )

        return reporter
    }

    @Bean
    fun sender(): Sender {
        return KafkaSender.newBuilder()
            .encoding(Encoding.PROTO3)
            .bootstrapServers(zipkinProperties.endpoint)
            // kafkaConfig properties 수정하는 경우 사용
            // .overrides()
            .build()
    }

    /**
     * @see https://armeria.dev/docs/advanced-zipkin/
     * @see org.springframework.boot.actuate.autoconfigure.tracing.BraveAutoConfiguration.braveTracing
     * @see org.springframework.boot.actuate.autoconfigure.tracing.BraveAutoConfiguration.braveCurrentTraceContext
     * Armeria 공식 문서에서 Tracing에 사용되는 currentTraceContext를 변경하라고 하기 때문에 그것만 변경
     */
    @Bean
    fun braveCurrentTraceContext(
        scopeDecorators: List<CurrentTraceContext.ScopeDecorator>,
        currentTraceContextCustomizers: List<CurrentTraceContextCustomizer>,
    ): CurrentTraceContext {

        val builder = RequestContextCurrentTraceContext.builder()
        scopeDecorators.forEach(
            Consumer { scopeDecorator: CurrentTraceContext.ScopeDecorator ->
                builder.addScopeDecorator(
                    scopeDecorator,
                )
            },
        )
        for (currentTraceContextCustomizer in currentTraceContextCustomizers) {
            currentTraceContextCustomizer.customize(builder)
        }

        return builder.build()
    }
}
