package com.example.grpc.service.team.dto

object TeamDto {

    data class CreateTeamRequest(
        val name: String,
        val requestedBy: String
    )
}
