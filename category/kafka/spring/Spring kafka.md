
> 본 포스팅은 spring boot 3.2.2 버전을 기준으로 작성되었습니다.

# 스프링 카프카
스프링 카프카는 카프카를 스프링 프레임워크에서 효과적으로 사용할 수 있도록 만들어진 라이브러리로 기존 카프카 클라이언트 라이브러리를 래핑해서 만들어졌습니다.

## 스프링 카프카 프로듀서
### 의존성
```groovy
implementation 'org.springframework.boot:spring-boot-starter'
implementation 'org.springframework.kafka:spring-kafka'
```

### application.yml

```yml
spring:
  kafka:
    bootstrap-servers: kafka.sample.url.com:9092
```

### KafkaTemplate과 ProducerFactory 설정

```kotlin
@Configuration
class CommonKafkaProducerConfig(
    private val kafkaProperties: KafkaProperties,
    private val sslBundles: SslBundles,
) {

    @Bean
    fun commonKafkaTemplate(): KafkaTemplate<String, Any> {
        return KafkaTemplate(commonProducerFactory())
    }

    @Bean
    fun commonProducerFactory(): ProducerFactory<String, Any> {
        val keySerializer = StringSerializer()
        val valueSerializer = JsonSerializer<Any>()

        return DefaultKafkaProducerFactory(kafkaProperties.buildProducerProperties(sslBundles), keySerializer, valueSerializer)
    }
}
```
스프링 카프카 프로듀서는 프로듀서 팩토리(ProducerFactory) 클래스를 사용하여 프로듀서의 설정값들을 세팅하고 카프카 템플릿(Kafka Template) 클래스를 사용하여 카프카 브로커로 메시지를 전송합니다.

스프링 카프카의 properties 설정값들은 kafkaProperties에서 관리됩니다. application.yml에 설정한 bootstrap-servers값도 kafkaProperties에 주입되어 관리되며, 따로 설정하지 않은 값들은 kafkaProperties에 설정된 기본값으로 세팅됩니다. ProducerFactory에서 보내고자 하는 메시지의 키와 값타입에 따른 serializer를 등록하고 해당 producerFactory를 kafkaTemplate의 인자로 사용하면 세팅이 완료되고 해당 kafkaTemplate을 사용하여 카프카 브로커로 메시지를 전송할 수 있습니다.

JsonSerializer를 등록할 때, objectMapper를 별도로 주입해줄 수 있지만, 주입하지 않으면 kafka에서 제공하는 JsonSerializer는 내부적으로 plain한 ObjectMapper가 아닌 enhancedObjectMapper 메서드를 사용합니다. javaTimeModule, unknownProperties false등 세팅 등 일반적으로 objectMapper를 별도로 빈으로 등록해서 사용해야하는 경우에 대한 세팅이 대부분 들어가 있기 때문에 이외의 추가적인 세팅이 필요한 경우가 아니라면 그대로 사용해도 무방합니다.

**리스너 설정**

```kotlin
@Bean
fun commonKafkaTemplate(): KafkaTemplate<String, Any> {
  return KafkaTemplate(commonProducerFactory()).apply {
    setProducerListener(CommonKafkaListener())
  }
}

class CommonKafkaListener : ProducerListener<String, Any> {

    private val log = KotlinLogging.logger { }

    override fun onError(producerRecord: ProducerRecord<String, Any>, recordMetadata: RecordMetadata?, exception: java.lang.Exception?) {
        log.error(
            "Fail to send kafka Message. Topic: ${producerRecord.topic()}, Partition: ${producerRecord.partition()}," +
                " Key: ${producerRecord.key()},  Value: ${producerRecord.value()}",
            exception,
        )
    }
}
```
ProducerListener를 구현하면 kafkaTemplate에 리스너를 붙여 사용할 수 있습니다. 

**actuator metric 설정**
```kotlin
@Configuration
class CommonKafkaProducerConfig(
  private val kafkaProperties: KafkaProperties,
  private val meterRegistry: MeterRegistry,
  private val sslBundles: SslBundles,
) {

  @Bean
  fun commonProducerFactory(): ProducerFactory<String, Any> {
    val keySerializer = StringSerializer()
    val valueSerializer = JsonSerializer<Any>()

    return DefaultKafkaProducerFactory(kafkaProperties.buildProducerProperties(sslBundles), keySerializer, valueSerializer)
      .apply { addListener(MicrometerProducerListener(meterRegistry)) }
  }
}
```
spring actuator를 사용한다면 producerFactory에 MicrometerProducerListener를 붙여서 모니터링을 할 수 있습니다. 

### KafkaTemplate 사용
kafkaTemplate의 send 메서드는 다양한 오버로딩을 제공합니다. 

+ send(String topic, V data)
+ send(String topic, K key, V data)
+ send(String topic, Integer partition, K key, V data)
+ send(String topic, Integer partition, Long timestamp, K key, V data)
+ send(ProducerRecord\<K,V> record)

topic, key, value를 각각 받아서 처리하는 함수도 있고 producerRecord를 받아서 처리하는 함수도 있지만 결국 내부적으로는 producerRecord를 만들어서 전송하게 됩니다.

```kotlin
data class Article(
    val id: String = UUID.randomUUID().toString(),
    val title: String = UUID.randomUUID().toString(),
    val attachment: List<Attachment> = listOf(Attachment()),
    val registeredDate: LocalDateTime = LocalDateTime.now(),
) {
    data class Attachment(
        val id: String = UUID.randomUUID().toString(),
        val path: String = UUID.randomUUID().toString(),
    )
}

data class KafkaMessage(
  val topic: String,
  val key: String,
  val data: Any,
  val headers: MutableMap<String, String> = mutableMapOf(),
) {
  fun buildProducerRecord(): ProducerRecord<String, Any> {
    return ProducerRecord(topic, key, data).apply {
      headers.entries.forEach {
        this.headers().add(it.key, it.value.toByteArray())
      }
    }
  }
}
```
위 객체를 예시로 한다면 아래와 같이 메시지를 발송할 수 있습니다.

```kotlin
@RestController
class SamplePublisher(
  private val commonKafkaTemplate: KafkaTemplate<String, Any>,
) {

  @PostMapping("/articles")
  fun publishMessage() {
    val messages = mutableListOf<KafkaMessage>()
    
    repeat(2) {
      val article = Article()
      messages.add(KafkaMessage(
        topic = "backtony-test",
        key = article.id,
        data = article,
      ))
    }

    for (message in messages) {
      commonKafkaTemplate.send(message.buildProducerRecord())
    }
  }
}
```

<br>


## 스프링 카프카 컨슈머
### 의존성
```groovy
implementation 'org.springframework.boot:spring-boot-starter'
implementation 'org.springframework.kafka:spring-kafka'
```

### application.yml
```yml
spring:
  kafka:
    bootstrap-servers: kafka.sample.url.com:9092
```

### commonKafkaListenerContainerFactory와 ConsumerFactory 설정
```kotlin
@EnableKafka // @KafkaListener 애노테이션 활성화
@Configuration
class ConsumerConfig(
  private val kafkaProperties: KafkaProperties,
  private val meterRegistry: MeterRegistry,
  private val sslBundles: SslBundles,
) {

  @Bean(COMMON)
  fun commonKafkaListenerContainerFactory(
    commonConsumerFactory: ConsumerFactory<String, Any>,
  ): KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Any>> {
    return ConcurrentKafkaListenerContainerFactory<String, Any>().apply {
      consumerFactory = commonConsumerFactory
    }
  }

  @Bean
  fun commonConsumerFactory(): ConsumerFactory<String, Any> {
    val keyDeserializer = StringDeserializer()
    val valueDeserializer = JsonDeserializer(Any::class.java).apply {
      addTrustedPackages("com.example.*") // JsonDeserializer 주의사항 파트에서 따로 설명
    }

    return DefaultKafkaConsumerFactory(getCommonConsumerConfigs(), keyDeserializer, valueDeserializer)
      .apply { addListener(MicrometerConsumerListener(meterRegistry)) }
  }

  private fun getCommonConsumerConfigs(): Map<String, Any> {
    return kafkaProperties.buildConsumerProperties(sslBundles)
  }

  companion object {
    const val COMMON = "commonKafkaListenerContainerFactory"
  }
}
```
스프링 카프카 컨슈머는 컨슈머 팩토리(ConsumerFactory) 클래스를 사용하여 컨슈머의 설정값들을 세팅하고 카프카 리스터 컨테이너 팩토리(KafkaListenerContainerFactory) 클래스를 브로커로부터 메시지를 수신합니다.

스프링 카프카의 properties 설정값들은 kafkaProperties에서 관리됩니다. application.yml에 설정한 bootstrap-servers값도 kafkaProperties에 주입되어 관리되며, 따로 설정하지 않은 값들은 kafkaProperties에 설정된 기본값으로 세팅됩니다. ConsumerFactory에서 수신하고자 하는 메시지 키와 값타입에 따른 deserializer를 등록하고 해당 ConsumerFactory를 KafkaListenerContainerFactory의 인자로 사용하면 세팅이 완료되고 카프카 브로커로부터 메시지를 수신할 수 있습니다.

컨슈머와 마찬가지로 spring actuator를 사용하는 경우 MicrometerConsumerListener를 추가하여 모니터링할 수 있습니다.

### @KafkaListener
```kotlin
@Configuration
class SampleListener {

    private val log = KotlinLogging.logger { }

    @KafkaListener(
        groupId = "backtony-test-single",
        topics = ["backtony-test"],
        containerFactory = COMMON,
    )
    fun sample(record: Article) {
        log.info { record }
    }

    @KafkaListener(
        groupId = "backtony-test-batch",
        topics = ["backtony-test"],
        containerFactory = COMMON,
        batch = "true",
    )
    fun sampleBatch(event: List<Article>) {
        log.info { "batch count : ${event.size}" }
    }
}
```
앞서 정의한 KafkaListenerContainerFactory 세팅을 통해서 카프카 브로커로부터 메시지를 받을 수 있는 구조가 만들어졌습니다. ConsumerConfig 클래스에 @EnableKafka 애노테이션을 붙였기 때문에 @KafkaListener를 사용할 수 있습니다. @KafkaListener 옵션으로 컨슈머 그룹, 토픽, containerFactory를 명시하면 카프카브로커로부터 메시지 소비가 시작됩니다. containerFactory에는 앞서 정의한 commonKafkaListenerContainerFactory의 빈 이름을 명시해주면 됩니다. batch 옵션은 메시지를 단건으로 받아서 처리할지, 다건(리스트)로 받아서 처리할지 여부를 의미합니다. 


### DefaultErrorHandler
컨슈머에서 로직을 처리하다가 문제가 발생했을 경우, 처리할 수 있도록 컨슈머에 CommonErrorHandler를 정의할 수 있고 여러 구현체가 제공됩니다. 보통 DefaultErrorHandler를 사용하게 됩니다.
```kotlin
public DefaultErrorHandler(@Nullable ConsumerRecordRecoverer recoverer, BackOff backOff) {
    this(recoverer, backOff, null);
}
```

여러 생성자가 있지만 위 생성자를 사용해보겠습니다. 예외가 발생했을 때, 수행할 동작을 정의하는 recoverer와 재시도 BackOff를 인자로 넘겨야 합니다.

```kotlin
@Component
class CommonConsumerRecordRecoverer : ConsumerRecordRecoverer {

    private val log = KotlinLogging.logger { }

    override fun accept(record: ConsumerRecord<*, *>, ex: Exception) {

        var groupId: String? = ""
        if (ex is ListenerExecutionFailedException) {
            groupId = ex.groupId
        }

        log.error(
            "[Consumer error] occurred error while consuming message. " +
                "topic : ${record.topic()}, groupId : $groupId, offset : ${record.offset()}, " +
                "key : ${record.key()}, value : ${record.value()}, error message : ${ex.message}",
            ex,
        )
    }
}
```
우선 ConsumerRecordRecoverer를 구현하여 예외가 발생했을 때, 로깅을 남기는 recoverer를 정의합니다.

<br>

```kotlin
@Bean(COMMON)
fun commonKafkaListenerContainerFactory(
  commonConsumerFactory: ConsumerFactory<String, Any>,
  commonErrorHandler: CommonErrorHandler,
): KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, Any>> {
  return ConcurrentKafkaListenerContainerFactory<String, Any>().apply {
    consumerFactory = commonConsumerFactory
    setCommonErrorHandler(commonErrorHandler) // 추가
  }
}

@Bean
fun commonErrorHandler(): CommonErrorHandler {
    return DefaultErrorHandler(commonConsumerRecordRecoverer, FixedBackOff(1000L, 3L)) // 1초 간격으로 최대 3회 재시도
}
```

그리고 KafkaListenerContainerFactory 정의 시점에 commonErrorHandler로 등록해주면 됩니다.



### JsonDeserializer 주의사항
프로듀서에서 jsonSerializer를 사용할 경우, 카프카 브로커로 메시지를 직렬화화여 전송할 때 kafkaHeader에 해당 객체의 타입 정보가 들어가게 됩니다. 그리고 컨슈머에서 이를 역직렬화하는 과정에서는 헤더에 들어있는 타입 정보를 사용하게 됩니다. 

프로젝트가 멀티모듈 구조인 경우, 프로듀서에서 발송한 메시지 객체를 컨슈머에서 공유해서 사용합니다. 이 경우에는 JsonDeserializer가 헤더에 들어있는 타입의 패키지 경로를 신뢰할 수 있도록 등록해줘야 합니다. 따라서 JsonDeserilizer를 등록할 때, 아래와 같이 카프카 프로듀서에서 전송한 객체의 패키지 경로를 신뢰할 수 있도록 등록해줘야 합니다.
```kotlin
val valueDeserializer = JsonDeserializer(Any::class.java).apply {
    addTrustedPackages("com.example.*")
  }
```

<br>

반면에 프로젝트가 MSA 환경이라 다른 팀에서 보낸 메시지를 우리 팀에서 수신해야 하는 경우가 있습니다. 이 경우에는 멀티모듈 구조와 달리 해당 객체를 공유해서 사용할 수 없습니다. 이 경우에는 2가지 방법이 있습니다.

1. 메시지 값타입을 String 값으로 받아서 @kafkaListener가 붙은 함수에서 objectMapper로 타입을 직접 변환하는 방식
2. JsonDeserilizer에서 헤더에 있는 타입을 사용하지 않는 방식. 

1번의 경우 는 ConsumerConfig에서 설정한 factory들의 value 타입을 String으로 바꾸고 @kafkaListener가 붙은 함수에서 직접 타입을 변환하고 처리하면 됩니다. 2번의 경우는 JsonDeserializer만 다음과 같이 변경하면 됩니다.

```kotlin
val valueDeserializer = JsonDeserializer(Any::class.java).apply {
  setUseTypeHeaders(false)
}
```
하지만 이 방식을 사용할 경우, @kafkaListener에서 바로 value 타입으로 받지 못하기 때문에 ConsumerRecord 형태로 인자를 받아서 처리해야 합니다.

```kotlin
@KafkaListener(
    groupId = "backtony-test-single",
    topics = ["backtony-test"],
    containerFactory = COMMON,
)
fun sample(record: ConsumerRecord<String, Article>) {
    log.info { record.value() }
}
```

### ErrorHandlingDeserializer
> 브로커로부터 직렬화된 데이터 수집 -> 데이터 역직렬화 -> 데이터 처리 -> 브로커에 commit 요청 

컨슈머는 대략 위와 같은 흐름으로 진행되고, 3번 과정인 데이터 처리에서 예외가 발생할 경우, 지정한 ErrorHandler에 의해 retry 횟수만큼 재시도하고 커밋하게 됩니다. 하지만 데이터 역직렬화 단계에서 실패한 경우 DeserializeException가 발생하면서 데이터 처리에서 발생한 예외가 아니기 때문에 ErrorHandler까지 도달하지 못하고 결국 commit되지 못해 같은 offset을 컨슈머가 계속 소비하게 되는 문제가 발생합니다. 

```java
public class ErrorHandlingDeserializer<T> implements Deserializer<T> {
    
    // .. 생략
    private Deserializer < T > delegate;

    public ErrorHandlingDeserializer (Deserializer<T> delegate) {
        this.delegate = setupDelegate(delegate);
    }
}
```
ErrorHandlingDeserializer가 이러한 문제를 해결합니다. ErrorHandlingDeserializer는 역직렬화의 처리를 delegate deserializer로 위임하고 역직렬화 실패 시, null을 반환하도록 설계되었습니다. 이를 통해, 결과는 null이지만 역직렬화 과정은 통과하여 데이터 처리 단계까지 도달할 수 있습니다.

```kotlin
@Bean
fun commonConsumerFactory(): ConsumerFactory<String, Any> {
    return DefaultKafkaConsumerFactory(getCommonConsumerConfigs(), StringDeserializer(), ErrorHandlingDeserializer(JsonDeserializer(Any::class.java))
}
```
사용 방법은 consumerFactory에 Deserilizer를 넘겨줄 때, ErrorHandlingDeserializer로 한번 감싸서 넘겨주면 됩니다.



### concurrency
KafkaListenerContainerFactory는 KafkaListenerContainerFactory, ConcurrentKafkaListenerContainerFactory 두가지 타입을 제공합니다.

![그림1](./1.png)

KafkaListenerContainerFactory는 단일 스레드로 동작합니다.(concurrency 옵션이 없습니다.) 따라서 소비해야할 메시지가 많은 경우, 컨슈머 랙이 발생할 수 있습니다.

![그림2](./2.png)

ConcurrentKafkaListenerContainerFactory는 멀티 스레드로 동작합니다.(concurrency 옵션이 있습니다.) 따라서 소비해야할 메시지가 많은 경우 적합합니다. 카프카 컨슈머 모델에서는 한 파티션을 동시에 여러 컨슈머 스레드가 처리할 수 없습니다. 따라서 파티션의 개수보다 스레드 수가 많아지면 나머지 스레드는 놀게 되면서 자원이 낭비되게 됩니다. 예를 들어, 파티션이 3개이고 concurrency가 5라면 나머지 2개의 스레드는 놀게 되면서 자원이 낭비되게 됩니다. 따라서 concurrency는 컨슈머에 매핑된 파티션의 개수보다 작거나 같아야 합니다.

```kotlin
@KafkaListener(
    groupId = "backtony-test-single",
    topics = ["backtony-test"],
    containerFactory = COMMON,
    concurrency = "3" // concurrency 설정
)
fun sample(record: Article) {
    log.info { record }
}
```

### 인터셉터

```kotlin
@Bean
fun commonConsumerFactory(): ConsumerFactory<String, Any> {
  return DefaultKafkaConsumerFactory(getCommonConsumerConfigs(), StringDeserializer(), getJsonValueDeserializer())
}

private fun getCommonConsumerConfigs(): Map<String, Any> {
  return kafkaProperties.buildConsumerProperties(sslBundles)
    .apply { put(ConsumerConfig.INTERCEPTOR_CLASSES_CONFIG, LoggingConsumerInterceptor::class.java.name) }
}

class LoggingConsumerInterceptor : ConsumerInterceptor<String, Any> {

    private val log = KotlinLogging.logger { }

    override fun configure(configs: MutableMap<String, *>) {}

    override fun close() {}

    override fun onCommit(offsets: MutableMap<TopicPartition, OffsetAndMetadata>?) {}

    override fun onConsume(records: ConsumerRecords<String, Any>): ConsumerRecords<String, Any> {
        records.forEach {
            log.info("Start consuming the message: ${it.value()}")
        }
        return records
    }
}
```
ConsumerInterceptor를 구현하여 인터셉터를 만들 수 있고 ConsumerFactory를 만들 때, config에 추가하면 적용할 수 있습니다.


