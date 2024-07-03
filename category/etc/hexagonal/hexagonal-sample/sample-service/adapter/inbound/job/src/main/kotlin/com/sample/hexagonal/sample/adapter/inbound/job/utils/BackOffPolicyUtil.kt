package com.sample.hexagonal.sample.adapter.inbound.job.utils

import org.springframework.retry.backoff.FixedBackOffPolicy

object BackOffPolicyUtil {

    fun createFixedBackOffPolicy(backOffPeriod: Long): FixedBackOffPolicy {
        return FixedBackOffPolicy().apply {
            this.backOffPeriod = backOffPeriod
        }
    }
}
