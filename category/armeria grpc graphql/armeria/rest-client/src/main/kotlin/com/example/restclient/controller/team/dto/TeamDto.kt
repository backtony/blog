package com.example.restclient.controller.team.dto

import java.time.LocalDateTime

object TeamDto {

    data class CreateTeamRequest(
        val name: String,
        val requestedBy: String,
    )

    data class TeamResponse(
        val id: Long,
        val name: String,
        val registeredBy: String,
        val registeredDate: LocalDateTime,
        val modifiedBy: String,
        val modifiedDate: LocalDateTime,
    )
}
