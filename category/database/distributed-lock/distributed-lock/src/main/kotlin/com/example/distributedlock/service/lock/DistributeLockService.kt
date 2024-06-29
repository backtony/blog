package com.example.distributedlock.service.lock

import com.example.distributedlock.utils.DistributedLockUtils
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class DistributeLockService(
    private val distributedLockUtils: DistributedLockUtils,
) {
    private val log = KotlinLogging.logger {  }


    @Transactional
    suspend fun lock() {
        distributedLockUtils.run("test", "1") {
            log.info { "test 1 called" }
        }
    }
}
