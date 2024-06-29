package com.example.springgraphql.config.zipkin

import brave.Tracing
import brave.grpc.GrpcTracing
import brave.http.HttpTracing
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

/**
 * @see org.springframework.boot.actuate.autoconfigure.tracing.zipkin
 * https://www.linkedin.com/pulse/tracing-spring-boot-3-zipkin-kafka-transport-%D0%BD%D0%B8%D0%BA%D0%B8%D1%82%D0%B0-%D0%BD%D0%BE%D1%81%D0%BE%D0%B2-errwf/
 */
@Configuration
class ZipkinConfiguration(
    private val zipkinProperties: ZipkinProperties,
) {

    private val log = KotlinLogging.logger { }

    @Bean
    fun httpTracing(tracing: Tracing): HttpTracing {
        return HttpTracing.create(tracing)
    }

    @Bean
    fun grpcTracing(tracing: Tracing): GrpcTracing {
        return GrpcTracing.create(tracing)
    }

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
}
