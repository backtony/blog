package com.sample.hexagonal.common.config

import io.micrometer.core.instrument.MeterRegistry
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.InetAddress
import java.net.UnknownHostException

@Configuration
class CustomMetricConfig {

    private val logger = KotlinLogging.logger { }

    @Bean
    fun metricsCommonTags(@Value("\${spring.application.name}") applicationName: String): MeterRegistryCustomizer<MeterRegistry> {
        return MeterRegistryCustomizer<MeterRegistry> { registry: MeterRegistry ->
            registry.config().commonTags("application", applicationName)
            try {
                registry.config().commonTags("host", InetAddress.getLocalHost().hostName)
            } catch (uhe: UnknownHostException) {
                logger.error("Error getting hostname", uhe)
            }
        }
    }
}
