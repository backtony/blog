package com.example.aopwithtransaction.controller.aop

import com.example.aopwithtransaction.service.aop.TargetService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class AopController(
    private val targetService: TargetService,
) {

    @GetMapping("/aop")
    suspend fun aop(): String {
        return targetService.aop()
    }
}
