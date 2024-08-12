package com.example.consumer.config

import io.micrometer.core.instrument.MeterRegistry
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.BytesDeserializer
import org.apache.kafka.common.serialization.Deserializer
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.kafka.common.utils.Bytes
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
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.converter.BatchMessagingMessageConverter
import org.springframework.kafka.support.converter.JsonMessageConverter
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer
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
        commonConsumerFactory: ConsumerFactory<String, Bytes>,
        commonErrorHandler: CommonErrorHandler,
    ): KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Bytes>> {
        return ConcurrentKafkaListenerContainerFactory<String, Bytes>().apply {
            consumerFactory = commonConsumerFactory
            setRecordMessageConverter(JsonMessageConverter())
            setBatchMessageConverter(BatchMessagingMessageConverter(JsonMessageConverter()))
            setCommonErrorHandler(commonErrorHandler)
        }
    }

//    @Bean(MANUAL_ACK)
//    fun manualAckKafkaListenerContainerFactory(
//        consumerFactory: ConsumerFactory<String, Bytes>,
//        commonErrorHandler: CommonErrorHandler,
//    ): ConcurrentKafkaListenerContainerFactory<String, Bytes> {
//        val factory = ConcurrentKafkaListenerContainerFactory<String, Bytes>()
//        factory.consumerFactory = consumerFactory
//        factory.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
//        factory.setCommonErrorHandler(commonErrorHandler)
//        factory.setRecordMessageConverter(JsonMessageConverter())
//        factory.setBatchMessageConverter(BatchMessagingMessageConverter(JsonMessageConverter()))
//        return factory
//    }

    @Bean
    fun commonConsumerFactory(): ConsumerFactory<String, Bytes> {
        return DefaultKafkaConsumerFactory(getCommonConsumerConfigs(), StringDeserializer(), getBytesValueDeserializer())
            .apply { addListener(MicrometerConsumerListener(meterRegistry)) }
    }

    private fun getBytesValueDeserializer(): Deserializer<Bytes> {
        return ErrorHandlingDeserializer(
            BytesDeserializer(),
        )
    }

    private fun getCommonConsumerConfigs(): Map<String, Any> {
        return kafkaProperties.buildConsumerProperties(sslBundles)
            .apply { put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, LoggingConsumerInterceptor::class.java.name) }
    }

    @Bean
    fun commonErrorHandler(): CommonErrorHandler {
        return DefaultErrorHandler(commonConsumerRecordRecoverer, FixedBackOff(1000L, 3L))
    }

    companion object {
        const val COMMON = "commonKafkaListenerContainerFactory"
        const val MANUAL_ACK = "manualAckKafkaListenerContainerFactory"
    }
}
