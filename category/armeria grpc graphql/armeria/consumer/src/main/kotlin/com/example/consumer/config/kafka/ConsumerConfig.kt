package com.example.consumer.config.kafka

import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.ssl.SslBundles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.MicrometerConsumerListener
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ConsumerRecordRecoverer
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.util.backoff.FixedBackOff

@EnableKafka
@Configuration
class ConsumerConfig(
    private val kafkaProperties: KafkaProperties,
    private val meterRegistry: MeterRegistry,
    private val sslBundles: SslBundles,
    private val commonConsumerRecordRecoverer: ConsumerRecordRecoverer,
) {

    @Bean(COMMON)
    fun commonKafkaListenerContainerFactory(
        commonConsumerFactory: ConsumerFactory<String, Any>,
        commonErrorHandler: CommonErrorHandler,
    ): KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Any>> {
        return ConcurrentKafkaListenerContainerFactory<String, Any>().apply {
            consumerFactory = commonConsumerFactory
            setCommonErrorHandler(commonErrorHandler)
            containerProperties.isObservationEnabled = true // zipkin
        }
    }

    @Bean
    fun commonConsumerFactory(): ConsumerFactory<String, Any> {
        return DefaultKafkaConsumerFactory(getCommonConsumerConfigs(), StringDeserializer(), getJsonValueDeserializer())
            .apply {
                addListener(MicrometerConsumerListener(meterRegistry))
            }
    }

    private fun getCommonConsumerConfigs(): Map<String, Any> {
        return kafkaProperties.buildConsumerProperties(sslBundles)
    }

    @Bean
    fun commonErrorHandler(): CommonErrorHandler {
        return DefaultErrorHandler(commonConsumerRecordRecoverer, FixedBackOff(1000L, 3L))
    }

    private fun getJsonValueDeserializer(): Deserializer<Any> {
        val jsonDeserializer = JsonDeserializer<Any>().apply {
            this.typeMapper = getJackson2JavaTypeMapper()
        }
        return ErrorHandlingDeserializer(jsonDeserializer)
    }

    private fun getJackson2JavaTypeMapper(): Jackson2JavaTypeMapper {
        return DefaultJackson2JavaTypeMapper()
            .apply {
                typePrecedence = Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID
                addTrustedPackages("com.example.event.*")
            }
    }

    companion object {
        const val COMMON = "commonKafkaListenerContainerFactory"
    }
}
