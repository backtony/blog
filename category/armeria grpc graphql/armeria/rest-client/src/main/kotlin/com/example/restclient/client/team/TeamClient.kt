package com.example.restclient.client.team

import com.example.proto.team.TeamHandlerGrpcKt
import com.example.restclient.controller.team.dto.TeamDto
import org.springframework.stereotype.Component

@Component
class TeamClient(
    private val teamServiceStub: TeamHandlerGrpcKt.TeamHandlerCoroutineStub
) {
    suspend fun createTeam(request: TeamDto.CreateTeamRequest): TeamDto.TeamResponse {
        return teamServiceStub.createTeam(TeamMapper.generateCreateTeamRequest(request))
            .let { TeamMapper.generateTeamResponse(it) }
    }
}
