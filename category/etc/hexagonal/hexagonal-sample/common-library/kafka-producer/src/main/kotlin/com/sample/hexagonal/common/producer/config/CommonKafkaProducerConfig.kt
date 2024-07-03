package com.sample.hexagonal.common.producer.config

import com.sample.hexagonal.common.json.ObjectMapperFactory
import com.sample.hexagonal.common.producer.handler.CommonKafkaListener
import com.sample.hexagonal.common.utils.yml.YamlPropertySourceFactory
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.ssl.SslBundles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.MicrometerProducerListener
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer

@PropertySource(
    "classpath:application-kafka-producer-\${spring.profiles.active}.yml",
    factory = YamlPropertySourceFactory::class,
)
@Configuration
class CommonKafkaProducerConfig(
    private val kafkaProperties: KafkaProperties,
    private val meterRegistry: MeterRegistry,
    private val sslBundles: SslBundles,
) {

    @Bean
    fun commonKafkaTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(commonProducerFactory()).apply {
            setProducerListener(CommonKafkaListener())
        }
    }

    @Bean
    fun commonProducerFactory(): ProducerFactory<String, Any> {
        val keySerializer = StringSerializer()
        val valueSerializer = JsonSerializer<Any>(ObjectMapperFactory.create())

        return DefaultKafkaProducerFactory(kafkaProperties.buildProducerProperties(sslBundles), keySerializer, valueSerializer)
            .apply { addListener(MicrometerProducerListener(meterRegistry)) }
    }
}
