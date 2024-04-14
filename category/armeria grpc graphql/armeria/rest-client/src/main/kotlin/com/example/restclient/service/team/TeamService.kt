package com.example.restclient.service.team

import com.example.restclient.client.team.TeamClient
import com.example.restclient.controller.team.dto.TeamDto
import org.springframework.stereotype.Service

@Service
class TeamService(
    private val teamClient: TeamClient,
) {

    suspend fun createTeam(request: TeamDto.CreateTeamRequest): TeamDto.TeamResponse {
        return teamClient.createTeam(request)
    }
}
