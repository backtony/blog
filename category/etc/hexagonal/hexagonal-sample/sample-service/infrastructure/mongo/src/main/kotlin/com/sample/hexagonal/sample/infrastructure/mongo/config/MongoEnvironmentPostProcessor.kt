package com.sample.hexagonal.sample.infrastructure.mongo.config

import org.springframework.boot.SpringApplication
import org.springframework.boot.env.EnvironmentPostProcessor
import org.springframework.boot.env.YamlPropertySourceLoader
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.io.ClassPathResource
import java.io.IOException

@Order(Ordered.LOWEST_PRECEDENCE)
class MongoEnvironmentPostProcessor(
    private val loader: YamlPropertySourceLoader = YamlPropertySourceLoader(),
) : EnvironmentPostProcessor {

    override fun postProcessEnvironment(environment: ConfigurableEnvironment, application: SpringApplication) {
        val activeProfile = when {
            environment.activeProfiles.any { it in listOf("test") } -> "test"
            else -> "dev"
        }
        val path = ClassPathResource("mongo/$activeProfile.yml")

        val propertySources = try {
            loader.load("mongo-resource", path)
        } catch (e: IOException) {
            throw IllegalStateException("Failed to load yaml configuration from $path")
        }

        if (propertySources.isNotEmpty()) {
            environment.propertySources.addLast(propertySources[0])
        }
    }
}
