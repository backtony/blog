## CircuitBreaker란?

![그림1](./1.png)

  
CircuitBreaker는 문제가 발생한 지점을 감지하고 실패하는 요청을 계속하지 않도록 방지하며, 이를 통해 시스템의 장애 확산을 막고 장애 복구를 도와주는 기능을 제공합니다. 위 그림과 같이 A가 B를 호출할 때, B가 반복적으로 실패한다면 CircuitBreaker를 Open 하여 B에 대한 흐름을 차단하는 기능을 제공합니다.

<br>

**CircuitBreaker를 지원하는 라이브러리**

-   Netflix Hystrix (deprecated)
    -   Netflix 에서 개발한 라이브러리로 MSA 환경에서 서비스 간 통신이 원활하지 않을 경우 각 서비스가 장애 내성과 지연 내성을 갖게 하는 라이브러리
-   Resilience4j
    -   Netflix Hystrix로 부터 영감을 받아 개발된 Fault Tolerance Library로 Java 전용으로 개발된 경량 라이브러리
    -   CircuitBreaker, Bulkhead, RateLimiter, Retry, TimeLimiter 등의 여러 가지 코어 모듈이 존재합니다.

<br>

## 상태

![그림2](./2.png)

CircuitBreaker는 3가지 상태가 있습니다.

-   closed : 정상
    -   정상적인 상태로 임계치가 넘어가면 OPEN 상태로 변경됩니다.
-   open : 장애
    -   장애 상태로 외부 요청을 차단하고 예외를 발생시키거나 fallback 함수를 호출합니다.
    -   장애 상태 판단 기준
        -   slow call : 기준보다 오래 걸린 요청
        -   failure call : 실패 혹은 오류 응답을 받은 요청
-   helf open : 장애 이후 임계치 재측정 상태
    -   open 상태가 된 이후 일정 요청 횟수/시간이 지난 뒤 open/closed 중 어떤 상태로 변경할지에 대한 판단이 다시 이뤄지는 상태입니다.


CircuitBreaker는 호출 결과를 저장하고 집계하기 위해 슬라이딩 윈도우를 사용합니다. 슬라이딩 윈도우는 마지막 N번의 호출 결과를 기반으로 하는 count-based sliding window(횟수 기반 슬라이딩 윈도우)와 마지막 N초의 결과를 기반으로 하는 time-based sliding window(시간 기반 슬라이딩 윈도우)가 있습니다.

<br>

느린 호출율과 호출 실패율이 서킷브레이커에 설정된 임계값보다 크거나 같다면 closed에서 open으로 상태가 변경됩니다. 모든 예외 발생은 실패로 간주(특정 예외만 예외 목록으로 지정하거나 ignore 등록 가능)됩니다. 일정 호출 수가 기록된 후에 느린 호출율과 호출 실패율이 계산됩니다.

<br>

CircuitBreaker는 서킷이 open 상태라면 CallNotPermittedException을 발생시킵니다. 그리고 특정 시간이 지나면 half open 상태로 바뀌고 설정된 수의 요청을 허용하여 동일하게 느린 호출율과 실패율에 따라 서킷의 상태를 open 또는 closed로 변경합니다.

<br>

Resilience4J는 일반 CircuitBreaker의 3가지 상태에 DISABLED와 FORCED_OPEN 이라는 2가지 상태를 추가로 지원합니다.

-   disabled : 서킷브레이커를 비활성화하여 항상 요청을 허용
-   forced open : 강제로 서킷을 열어 항상 요청을 거부하는 상태

<br>

## Resilience4j Property 옵션
<br>

### close -> open

-   failureRateThreshold
    -   기본값 : 50
    -   실패율 임계치 백분율로 해당 값을 넘어갈 경우 open 상태로 전환
-   slowCallDurationThreshold
    -   기본값 : 600000 ms
    -   해당 설정값을 넘어서는 경우 slow call로 판단
-   slowCallRateThreshold
    -   기본값 : 100
    -   slow call 임계값 백분율로 넘어가면 open 상태로 변경

<br>

### open -> half open

-   waitDurationInOpenState
    -   기본값 : 600000ms
    -   open 상태에서 half open 상태로 변경 대기 시간
-   automaticTransitionFromOpenToHalfOpenEnabled
    -   기본값 : false
    -   true이면 시간 동안 대기하지 않고 half open으로 전환

<br>

### half open

-   permittedNumberOfCallsInHalfOpenState
    -   half open 상태일 때 허용할 call 개수

<br>

### half open -> open

-   maxWaitDurationInHalfOpenState
    -   기본값 : 0
    -   half open 상태에서 open 상태로 변경되기 전까지 최대 유지 시간
    -   0인 경우 일부 허용된 call이 완료될 때까지 대기

<br>

### sliding window

-   slidingWindowType
    -   기본값 : COUNT_BASED
    -   요청 결과를 기록할 sliding window 타입으로 COUNT_BASED, TIME_BASED 중 선택
    -   count based는 slidingWindowSize 요청 중 실패율이 설정된 임계값을 초과하면, time based는 slidingWindowSize 시간 동안 실패율이 설정된 임계값을 초과하면 서킷브레이커가 동작
-   slidingWindowSize
    -   기본값 : 100
    -   sliding window 크기로 count_based 인 경우 개수, time_Based인 경우 초
-   minimumNumberOfCalls
    -   기본값 : 100
    -   failureRate, slowCallRate 비율을 계산하기 위한 최소 call 개수
    -   기본값이 100이라면 99번까지 실패해도 circuitBreaker가 동작하지 않음.
-   recordExceptions
    -   실패로 기록할 Exception 리스트 (기본값: empty)
-   ignoreExceptions
    -   실패나 성공으로 기록하지 않을 Exception 리스트 (기본값: empty)
-   recordFailurePredicate
    -   기본값 : throwable -> true
    -   failure로 집계할 exception인지 판단할 predicate
-   ignoreExceptionPredicate
    -   기본값 : throwable -> false
    -   failure로 집계하지 않을 exception인지 판단할 predicate

<br>

## spring 설정

### build.gradle

```groovy
implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j")
implementation("org.springframework.boot:spring-boot-starter-aop")
```

aop와 resilience4j 의존성을 추가합니다. aop는 annotation 방식을 사용하기 위해서 필요합니다.

<br>

### application.yml

```yml
# @CircuitBreaker name에 지정된 서킷브레이커가 없으면 default 설정을 가져온 해당 이름의 서킷브레이커를 만든다.
resilience4j:
  circuitbreaker:
    configs:
      default:
        minimum-number-of-calls: 5   # 집계에 필요한 최소 호출 수
        sliding-window-size: 5   # 서킷 CLOSE 상태에서 N회 호출 도달 시 failureRateThreshold 실패 비율 계산
        failure-rate-threshold: 10   # 실패 10% 이상 시 서킷 오픈
        slow-call-duration-threshold: 500   # 500ms 이상 소요 시 실패로 간주
        slow-call-rate-threshold: 10   # slowCallDurationThreshold 초과 비율이 10% 이상 시 서킷 오픈
        wait-duration-in-open-state: 10000   # OPEN -> HALF-OPEN 전환 전 기다리는 시간
        permitted-number-of-calls-in-half-open-state: 5   # HALFOPEN -> CLOSE or OPEN 으로 판단하기 위해 호출 횟수
```

<br>

### @CircuitBreaker 애노테이션 방식

```kotlin
@RestController
class ArticleController(
    private val articleService: ArticleService
) {

    // fallback은 본 함수와 인자가 일치해야함.
    @CircuitBreaker(name = "article", fallbackMethod = "failSample")
    @GetMapping("/v1/articles/{id}")
    fun getSampleArticle(): Article {
        val list = listOf(
            IllegalStateException("illegalState"),
            IllegalArgumentException("illegalArgument"),
        )
        throw list.random()
    }

    // IllegalArgumentException이 발생했을 경우 호출
    private fun failSample(throwable: IllegalArgumentException): Article {
        return Article("IllegalArgumentException title", "IllegalArgumentExceptionfail body")
    }

    // IllegalStateException이 발생했을 경우 호출
    private fun failSample(e: IllegalStateException): Article {
        return Article("IllegalStateException title", "IllegalStateExceptionfail body")
    }
}
```

@circuitBreaker의 name 속성의 값으로 application.yml에 설정이 등록되어있지 않다면 application.yml의 default 옵션 세팅으로 name 속성의 circuitBreaker가 생성됩니다. fallbackMethod 속성에 메서드명를 명시하면 특정 예외가 발생했을 때, 호출될 메서드를 지정하여 응답을 대신할 수 있습니다. 서킷이 open 상태로 바뀌면 더 이상 요청은 전달되지 않고 차단되며 CallNotPermittedException 예외가 발생합니다. 이 경우에도 마찬가지로 CallNotPermittedException을 받아서 처리하는 failSample 함수를 구현해서 처리할 수도 있습니다.

<br>

### 애노테이션 방식 개선하기
<br>

#### 문제점

@CircuitBreaker 애노테이션 방식에서는 아래와 같은 문제점이 있습니다.

-   런타임 예외 가능성
    -   fallbackMethod 속성의 값을 잘못 명시하더라도 컴파일 시점에 알 수 없습니다.
-   낮은 응집도
    -   실패 시, 여러 fallback 중 어떤 fallback이 동작하는지는 메서드명을 보고 찾아야 하기 때문에 한눈에 들어오지 않습니다.
-   구현체를 알아야만 한다.
    -   circuitBreaker가 open되었을 때 발생하는 CallNotPermittedException 예외가 resilience4j에서 만든 예외이기 때문에 circuitBreaker를 사용하는 함수 입장에서 resilience4j 구현체를 직접 알아야만 합니다.
-   open fallback 처리의 번거로움
    -   서킷이 open 되었을 때뿐만 아니라, 함수에서 예외가 발생하면 항상 fallback으로 넘어오기 때문에 서킷 open으로 넘어온 것인지 일반적인 예외로 넘어온 것인지 확인하는 과정이 필요합니다.
-   동일한 클래스의 내부 함수 호출 불가능
    -   spring aop가 가지고 있는 일반적인 문제

<br>

#### 개선하기

```kotlin
interface CircuitBreaker {
    fun <T> run(name: String, block: () -> T): Result<T>
}

@Component
class DefaultCircuitBreaker(
    private val factory: CircuitBreakerFactory<*, *>,
) : CircuitBreaker {

    override fun <T> run(name: String, block: () -> T): Result<T> = runCatching {
        factory.create(name).run(block) { e -> throw e.convertToCustomException() }
    }
}
```

-   DefaultCircuitBreaker의 역할
    -   spring에서 제공하는 circuitBreakerFactory는 create 메서드로 circuitBreaker 인스턴스를 만들고, run 메서드에 실행할 함수를 인자로 줄 수 있습니다. 두 번째 인자로 예외가 발생했을 경우 처리할 함수를 지정해 줄 수도 있습니다.
    -   DefaultCircuitBreaker 클래스의 목적은 spring에서 제공하는 CircuitBreakerFactory를 직접 사용하지 않고 한번 감추기 위함입니다.
-   Result 타입
    -   리턴타입을 Result 클래스로 한번 감싸는 이유는 이후 구현할 fallback에서 체이닝을 하기 위해서입니다.
-   convertToCustomException
    -   CircuitBreaker를 사용하는 곳에서는 resilience4j 구현체를 몰라도 되도록 resilience4j 예외인 CallNotPermittedException를 다른 customException으로 변경하는 역할을 제공합니다.


```kotlin
class CircuitBreakerProvider(
    circuitBreaker: CircuitBreaker,
) {
    init {
        Companion.circuitBreaker = circuitBreaker
    }

    companion object {
        private lateinit var circuitBreaker: CircuitBreaker
        fun get() = circuitBreaker
    }
}
```

```kotlin
@Configuration
class CircuitBreakerConfig {

    @Bean
    fun circuitBreakerProvider(
        circuitBreaker: CircuitBreaker,
    ) = CircuitBreakerProvider(circuitBreaker)
}
```

CircuitBreakerProvider 클래스의 역할은 spring application이 뜰 때, circuitBreaker singleton 인스턴스를 하나 받아서 가지고 있다가 필요할 때 전달해 주는 용도입니다. 이후 circuit util 함수를 구현하기 위해서 circuitBreaker 인스턴스를 전달해주는 역할을 합니다. 

이제 애노테이션 방식의 문제점을 해결할 circuitBreaker util 기능을 구현할 차례입니다.

```kotlin
/**
 * @param name 서킷 브레이커의 이름으로, 서킷 브레이커 인스턴스를 구별하는 데 사용
 * @param circuitBreaker 실행할 함수를 보호할 서킷 브레이커 인스턴스
 * @param f 실행할 함수. 이 함수는 서킷 브레이커의 하위에서 실행
 */
fun <T> circuit(
    name: String = "default",
    circuitBreaker: CircuitBreaker = CircuitBreakerProvider.get(),
    f: () -> T,
): Result<T> = circuitBreaker.run(name, f)
```

-   name : circuitBreaker 인스턴스를 구별하는 데 사용되는 서킷 브레이커 이름을 명시합니다.
-   circuitBreaker : 실행할 함수를 보호할 서킷 브레이커 인스턴스
    -   앞서 spring application이 뜰 때, circuitBreaker에서 singleton 인스턴스를 하나 받아서 가지고 있는 이유가 이 util 클래스에서 사용하기 위함입니다.
-   f : circuitBreaker에 감싸져서 실행될 실제 target 함수입니다.

```kotlin
class CircuitOpenException(message: String = "Circuit breaker is open") : RuntimeException(message)

fun Throwable.convertToCustomException(): Throwable = when (this) {
    is CallNotPermittedException -> CircuitOpenException()
    else -> this
}

fun <T> Result<T>.fallback(f: (e: Throwable?) -> T): Result<T> = when (this.isSuccess) {
    true -> this
    false -> runCatching { f(this.exceptionOrNull()) }
}

fun <T> Result<T>.fallbackIfOpen(f: (e: Throwable?) -> T): Result<T> = when (this.exceptionOrNull()) {
    is CircuitOpenException -> runCatching { f(this.exceptionOrNull()) }
    else -> this
}
```

-   convertToCustomException
    -   앞서 DefaultCircuitBreaker 클래스 정의 부분에서 설명한 CallNotPermittedException 예외를 CustomException으로 변경하는 함수입니다.
-   CircuitOpenException
    -   CallNotPermittedException를 대신할 custom exception입니다.
-   fallback
    -   성공인 경우에는 그대로 리턴하고, 실패한 경우에는 전달받은 fallback용 block을 실행하고 result로 감싸서 응답합니다.
    -   즉, 실패 예외가 어떤 것이든 fallback 동작을 수행합니다.
-   fallbackIfOpen
    -   Result에서 꺼낸 exception이 CircuitOpenException인 경우에만 전달받은 fallback block을 실행하고 이외의 경우에는 result를 그대로 응답합니다.
    -   즉, circuitBreaker가 Open인 경우에만 fallback으로 전달한 동작이 수행됩니다.

```kotlin
// 사용 예시
@RestController
class CircuitUtilController() {

    @GetMapping("/util/articles/fallback")
    fun getFallbackSampleArticle(): Article {
        return circuit("fallback-article") {
            throw RuntimeException("runtime")
        }.fallback {
            Article("Fallback title", "Fallback body")
        }.getOrThrow()
    }

    @GetMapping("/util/articles/open")
    fun getFallbackOpenSampleArticle(): Article {
        return circuit("fallback-open-article") {
            throw RuntimeException("runtime")
        }.fallbackIfOpen {
            Article("Fallback Open Default title", "Fallback Open Default body")
        }.getOrThrow()
    }
}
```

**참고**

* [서킷브레이커 사용 방식 개선하기](https://www.youtube.com/watch?v=ThLfHtoEe1I)