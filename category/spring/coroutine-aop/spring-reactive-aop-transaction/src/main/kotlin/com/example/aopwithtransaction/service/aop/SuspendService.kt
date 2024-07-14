package com.example.aopwithtransaction.service.aop

import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.stereotype.Service

@Service
class SuspendService {

    private val log = KotlinLogging.logger {  }

    suspend fun intercept(msg: String) {
        delay(100)
        log.info { msg }
    }
}
