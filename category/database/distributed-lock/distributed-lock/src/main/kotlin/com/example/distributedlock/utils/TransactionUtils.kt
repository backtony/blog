package com.example.distributedlock.utils

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Component
class TransactionUtils {

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    suspend fun <T> executeInNewTransaction(
        operation: suspend () -> T,
    ): T {
       return operation()
    }
}
