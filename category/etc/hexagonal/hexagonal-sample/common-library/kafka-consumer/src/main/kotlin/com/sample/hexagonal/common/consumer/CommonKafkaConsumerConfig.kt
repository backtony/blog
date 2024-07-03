package com.sample.hexagonal.common.consumer

import com.sample.hexagonal.common.json.ObjectMapperFactory
import com.sample.hexagonal.common.utils.yml.YamlPropertySourceFactory
import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.autoconfigure.kafka.KafkaProperties
import org.springframework.boot.ssl.SslBundles
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.PropertySource
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.config.KafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.core.MicrometerConsumerListener
import org.springframework.kafka.listener.CommonErrorHandler
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer
import org.springframework.kafka.listener.ConsumerRecordRecoverer
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.mapping.DefaultJackson2JavaTypeMapper
import org.springframework.kafka.support.mapping.Jackson2JavaTypeMapper
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
import org.springframework.kafka.support.serializer.JsonDeserializer
import org.springframework.util.backoff.FixedBackOff

@PropertySource(
    "classpath:application-kafka-consumer-\${spring.profiles.active}.yml",
    factory = YamlPropertySourceFactory::class,
)
@EnableKafka
@Configuration
class CommonKafkaConsumerConfig(
    private val kafkaProperties: KafkaProperties,
    private val meterRegistry: MeterRegistry,
    private val sslBundles: SslBundles,
    private val commonConsumerRecordRecoverer: ConsumerRecordRecoverer,
) {

    @Bean(KafkaListenerContainFactoryNames.COMMON)
    fun commonKafkaListenerContainerFactory(
        @Qualifier("commonConsumerFactory") commonConsumerFactory: ConsumerFactory<String, Any>,
        commonErrorHandler: CommonErrorHandler,
    ): KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Any>> {
        return ConcurrentKafkaListenerContainerFactory<String, Any>().apply {
            consumerFactory = commonConsumerFactory
            setCommonErrorHandler(commonErrorHandler)
        }
    }

    @Bean(KafkaListenerContainFactoryNames.MANUAL_ACK)
    fun manualAckKafkaListenerContainerFactory(
        @Qualifier("manualAckConsumerFactory") consumerFactory: ConsumerFactory<String, Any>,
        commonErrorHandler: CommonErrorHandler
    ): ConcurrentKafkaListenerContainerFactory<String, *> {
        val factory = ConcurrentKafkaListenerContainerFactory<String, Any>()
        factory.consumerFactory = consumerFactory
        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
        factory.setCommonErrorHandler(commonErrorHandler)
        return factory
    }

    @Bean("commonConsumerFactory")
    fun commonConsumerFactory(): ConsumerFactory<String, Any> {
        return DefaultKafkaConsumerFactory(getCommonConsumerConfigs(), StringDeserializer(), getJsonValueDeserializer())
            .apply { addListener(MicrometerConsumerListener(meterRegistry)) }
    }

    @Bean("manualAckConsumerFactory")
    fun manualAckConsumerFactory(): ConsumerFactory<String, Any> {
        return DefaultKafkaConsumerFactory(getManualAckConsumerConfigs(), StringDeserializer(), getJsonValueDeserializer())
            .apply { addListener(MicrometerConsumerListener(meterRegistry)) }
    }

    @Bean
    fun commonErrorHandler(): CommonErrorHandler {
        return DefaultErrorHandler(commonConsumerRecordRecoverer, FixedBackOff(1000L, 3L))
    }

    private fun getJsonValueDeserializer(): Deserializer<Any> {
        val jsonDeserializer = JsonDeserializer<Any>(ObjectMapperFactory.create()).apply {
            this.typeMapper = getJackson2JavaTypeMapper()
        }
        return ErrorHandlingDeserializer(jsonDeserializer)
    }

    private fun getJackson2JavaTypeMapper(): Jackson2JavaTypeMapper {
        return DefaultJackson2JavaTypeMapper()
            .apply {
                typePrecedence = Jackson2JavaTypeMapper.TypePrecedence.TYPE_ID
                addTrustedPackages("com.sample.hexagonal.*")
            }
    }

    private fun getCommonConsumerConfigs(): Map<String, Any> {
        return kafkaProperties.buildConsumerProperties(sslBundles)
            .apply { put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, LoggingConsumerInterceptor::class.java.name) }
    }

    private fun getManualAckConsumerConfigs(): Map<String, Any> {
        return kafkaProperties.buildConsumerProperties(sslBundles)
            .apply {
                put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, LoggingConsumerInterceptor::class.java.name)
                put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false")
            }
    }
}
