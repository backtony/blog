package com.example.mongo.domain

import java.time.LocalDateTime

class Pipeline(
    val id: String? = null,
    val steps: List<Step>,
    var status: Status = Status.WAIT,
    val registeredBy: String = "-",
    val registeredDate: LocalDateTime = LocalDateTime.now(),
) {

    data class Step(
        val type: String,
        val status: Status = Status.WAIT,
        val registeredBy: String= "-",
        val registeredDate: LocalDateTime = LocalDateTime.now(),
    ) {

        enum class Status {
            WAIT,
            DONE,
            FAIL,
        }
    }

    enum class Status {
        WAIT,
        DONE,
        FAIL,
    }
}
