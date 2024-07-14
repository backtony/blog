package com.example.aopwithtransaction.service.aop

import com.example.aopwithtransaction.aop.Logging
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TargetService() {
    private val log = KotlinLogging.logger { }

    @Logging
    @Transactional
    suspend fun aop(): String {
        delay(100)
        log.info { "target method call" }

        return "ok"
    }
}
