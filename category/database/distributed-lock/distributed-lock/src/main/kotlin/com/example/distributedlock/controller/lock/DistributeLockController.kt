package com.example.distributedlock.controller.lock

import com.example.distributedlock.service.lock.DistributeLockService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class DistributeLockController(
    private val distributeLockService: DistributeLockService
) {

    @GetMapping("/lock")
    suspend fun lock() {
        distributeLockService.lock()
    }
}
