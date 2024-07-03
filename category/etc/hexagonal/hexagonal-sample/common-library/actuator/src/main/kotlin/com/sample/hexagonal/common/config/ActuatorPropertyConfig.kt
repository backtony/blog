package com.sample.hexagonal.common.config

import com.sample.hexagonal.common.utils.yml.YamlPropertySourceFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource

@Configuration
@PropertySource(
    "classpath:application-actuator.yml",
    factory = YamlPropertySourceFactory::class,
)
class ActuatorPropertyConfig
