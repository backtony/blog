package com.example.distributedlock.config.redis

import io.lettuce.core.TimeoutOptions
import io.lettuce.core.cluster.ClusterClientOptions
import io.lettuce.core.cluster.ClusterTopologyRefreshOptions
import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.springframework.boot.autoconfigure.data.redis.RedisProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.connection.RedisStandaloneConfiguration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.connection.lettuce.LettucePoolingClientConfiguration
import java.time.Duration

// docker run -d -p 6379:6379 --name redis redis
// docker exec -it redis redis-cli
@Configuration
class RedisConfig(
    private val redisProperties: RedisProperties,
) {

    // https://godekdls.github.io/Spring%20Integration/messaging-endpoints/
    // Spring Integration에서 locks 패키지 LockRegistry를 제공하는 분산락
    // spring integration에서 제공되는 것을 사용하면 redisson을 사용하지 않아도 된다.
    // https://github.com/spring-projects/spring-integration/issues/8630
    // 하지만 위 이슈를 보면 코루틴에 대해서는 아직 제공하지 않는 것으로 보인다.
    // 실제 테스트 했을때, default 디스패처에서는 문제가 없는데(이것도 정확하진 않음), io 디스패처에서 문제가 있음.
    // redission 라이브러리에서 reactive한 방삭이 제공되므로 굳이 사용할 필요는 없을듯?
//    @Bean
//    fun redisLockRegistry(): RedisLockRegistry {
//        return RedisLockRegistry(redisConnectionFactory(), "REDIS_LOCK", 60_000L)
//    }

    // 그래서 다른 라이브러리인 redisson을 사용한다.
    /**
     * config 정보
     * @see org.redisson.spring.starter.RedissonAutoConfiguration
     */
    // https://github.com/redisson/redisson
    @Bean
    fun redissonClient(): RedissonClient {
        val config = Config()
        val poolConfig = lettucePoolingClientConfiguration().poolConfig
        config.useSingleServer().setAddress("redis://${redisProperties.url}:${redisProperties.port}")
            .setConnectionPoolSize(poolConfig.maxTotal)
            .setConnectionMinimumIdleSize(poolConfig.minIdle)
            .setTimeout(redisProperties.timeout.toMillis().toInt())
            .setConnectTimeout(redisProperties.connectTimeout.toMillis().toInt())
            .setIdleConnectionTimeout(3_000)
        return Redisson.create(config)
    }

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        return LettuceConnectionFactory(
            RedisStandaloneConfiguration(redisProperties.url, redisProperties.port),
            lettucePoolingClientConfiguration(),
        )
        // 아래는 클러스터 설정
//        return LettuceConnectionFactory(RedisClusterConfiguration(listOf(redisProperties.url)), lettuceClientConfiguration)
    }

    private fun lettucePoolingClientConfiguration(): LettucePoolingClientConfiguration {
        val topologyRefreshOptions = ClusterTopologyRefreshOptions.builder()
            .enableAllAdaptiveRefreshTriggers()
            .enablePeriodicRefresh(Duration.ofSeconds(30)) // 기본값이 disabled이므로 설정 필수, 권장 주기 30초
            .dynamicRefreshSources(true) // 기본값이 true이므로 설정하지 않아도 되지만 false로 변경은 금지
            .build()


        val timeoutOptions = TimeoutOptions.builder()
            .timeoutCommands()
            .fixedTimeout(redisProperties.timeout) // 사용 용도에 따라 자유롭게 설정
            .build()

        val clientOptions = ClusterClientOptions.builder()
            .topologyRefreshOptions(topologyRefreshOptions)
            .timeoutOptions(timeoutOptions)
            .build()

        return LettucePoolingClientConfiguration.builder()
            .clientOptions(clientOptions)
            .poolConfig(GenericObjectPoolConfig<Any>())
            .build()
    }
}
