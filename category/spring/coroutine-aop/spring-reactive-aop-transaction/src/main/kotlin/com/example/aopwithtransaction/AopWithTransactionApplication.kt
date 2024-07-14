package com.example.aopwithtransaction

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class AopWithTransactionApplication

fun main(args: Array<String>) {
    runApplication<AopWithTransactionApplication>(*args)
}
