package com.example.r2dbc.domain

import com.example.r2dbc.utils.memoizeSuspendNullable
import java.time.LocalDateTime

class Member(
    val id: Long? = null,
    val name: String,
    introduction: String? = null,
    val type: Type,
    teamId: Long? = null,
    val registeredBy: String,
    val registeredDate: LocalDateTime = LocalDateTime.now(),
    modifiedBy: String,
    val modifiedDate: LocalDateTime = LocalDateTime.now(),
    teamProvider: suspend () -> Team? = { null },
) {
    var teamId: Long? = teamId
        private set

    var introduction: String? = introduction
        private set

    var modifiedBy: String = modifiedBy
        private set

    val getTeam = memoizeSuspendNullable { teamProvider() }

    fun update(teamId: Long?, introduction: String?, requestedBy: String) {
        this.teamId = teamId
        this.introduction = introduction
        this.modifiedBy = requestedBy
    }

    enum class Type {
        INDIVIDUAL,
        COMPANY,
    }
}
