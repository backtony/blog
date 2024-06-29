package com.example.distributedlock.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import org.redisson.api.RedissonReactiveClient
import org.springframework.stereotype.Component
import java.security.SecureRandom
import java.util.concurrent.TimeUnit
import kotlin.time.DurationUnit
import kotlin.time.toDuration

// reentrance 락은 락을 획득한 스레드는 락에 재진입이 가능한데 코루틴의 경우, 여러 코루틴이 스레드풀을 공유하는 경우, 같은 스레드가 할당되게 되면 걸린 락에 재진입이 가능한 경우가 발생한다.
// semaphore은 여러개의 접근을 가능하도록 할 때 사용한다. ex) 2명까지 접근 가능하도록. => 1명만 가능하도록 할 수 있지만 try_lock_time_out 설정이 불가능
@Component
class DistributedLockUtils(
    private val redissonReactiveClient: RedissonReactiveClient,
    private val transactionUtils: TransactionUtils,
) {
    private val log = KotlinLogging.logger {}

    // https://betterprogramming.pub/dont-use-java-redissonclient-with-kotlin-coroutines-for-distributed-redis-locks-41da2e85c54a
    // 위 링크에서는 coroutines을 사용할때, redisson 사용 시 고려할 사항이 작성되어있다.
    // Redisson 락은 락을 획득한 스레드에서만 해제할 수 있다.
    // 코루틴은 락을 획득한 스레드와 락을 해제하려고 하는 스레드가 다를 수 있기 때문에 threadId를
    // uuid로 명시해서 획득할때와 해제할때 같은 id를 넣어주도록 하여 이를 우회할 수 있다.
    suspend fun <T> run(targetClassName: String, id: String, block: suspend () -> T): T {
        return withContext(Dispatchers.IO) {
            val uniqueId = SecureRandom().nextLong()
            val lockName = "$targetClassName:$id"
            val lock = redissonReactiveClient.getLock(lockName)

            log.info { "Try to get lock" }
            val available = lock.tryLock(TRY_LOCK_TIME_OUT, LEASE_TIME, TimeUnit.SECONDS, uniqueId).awaitSingle()
            log.info { "Get lock result status : $available" }

            check(available) { "Fail to get lock $lockName. Acquire lock timeout." }

            try {
                withTimeout(TARGET_METHOD_TIME_OUT.toDuration(DurationUnit.SECONDS)) {
                    transactionUtils.executeInNewTransaction { block.invoke() }
                }
            } catch (ex: Exception) {
                when (ex) {
                    is TimeoutCancellationException -> throw IllegalStateException(
                        "Lock lease time expired. LockName : $lockName ", ex
                    )

                    else -> throw ex
                }
            } finally {
                withContext(NonCancellable) {
                    if (available) {
                        log.info { "Release lock" }
                        lock.unlock(uniqueId).awaitSingleOrNull()
                    }
                }
            }
        }
    }

    companion object {
        // 획득까지 대기 시간
        private const val TRY_LOCK_TIME_OUT = 7L

        // 획득 이후 잡고 있을 시간, 이 시간이 지나도 unlock되지 않으면 자동으로 unlock
        private const val LEASE_TIME = 5L

        private const val TARGET_METHOD_TIME_OUT = 3L
    }
}
