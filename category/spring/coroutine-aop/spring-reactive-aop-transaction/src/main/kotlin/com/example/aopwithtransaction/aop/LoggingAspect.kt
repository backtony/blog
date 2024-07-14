package com.example.aopwithtransaction.aop

import com.example.aopwithtransaction.service.aop.SuspendService
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

//@Order(1)
@Aspect
@Component
class LoggingAspect(
    private val suspendService: SuspendService,
) {

    @Around("@annotation(com.example.aopwithtransaction.aop.Logging)")
    fun logging(joinPoint: ProceedingJoinPoint): Any? {
        return mono {
            suspendService.intercept("Aop Logging started")

            val result = joinPoint.proceed().let { result ->
                if (result is Mono<*>) {
                    result.awaitSingleOrNull()
                } else {
                    result
                }
            }

            suspendService.intercept("Aop Logging completed")

            result
        }
    }
}
