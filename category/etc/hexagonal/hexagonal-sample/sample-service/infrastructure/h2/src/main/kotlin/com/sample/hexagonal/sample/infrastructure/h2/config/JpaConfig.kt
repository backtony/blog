package com.sample.hexagonal.sample.infrastructure.h2.config

import com.sample.hexagonal.common.utils.yml.YamlPropertySourceFactory
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@PropertySource(
    "classpath:application-h2-\${spring.profiles.active}.yml",
    factory = YamlPropertySourceFactory::class,
)
@EnableJpaRepositories(
    basePackages = [
        "com.sample.hexagonal.sample.infrastructure.h2",
    ],
)
@EntityScan(basePackages = ["com.sample.hexagonal.sample.infrastructure.h2"])
@EnableJpaAuditing
class JpaConfig
