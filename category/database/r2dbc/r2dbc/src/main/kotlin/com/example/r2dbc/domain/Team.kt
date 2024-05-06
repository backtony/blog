package com.example.r2dbc.domain

import java.time.LocalDateTime

class Team(
    val id: Long? = null,
    val name: String,
    val registeredBy: String,
    val registeredDate: LocalDateTime = LocalDateTime.now(),
    modifiedBy: String,
    val modifiedDate: LocalDateTime = LocalDateTime.now(),
) {

    var modifiedBy: String = modifiedBy
        private set
}
