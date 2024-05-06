package com.example.r2dbc.domain

import com.example.r2dbc.utils.AsyncLazy
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
    private val teamProvider: suspend () -> Team? = { null },
) {
    private val cachedTeam = AsyncLazy { teamProvider() }

    var teamId: Long? = teamId
        private set

    var introduction: String? = introduction
        private set

    var modifiedBy: String = modifiedBy
        private set

    suspend fun getTeam() = cachedTeam.get()

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
