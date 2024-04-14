package com.example.grpc.handler.team

import com.example.grpc.config.GrpcHandler
import com.example.grpc.service.team.TeamService
import com.example.proto.team.CreateTeamRequest
import com.example.proto.team.TeamHandlerGrpcKt
import com.example.proto.team.TeamResponse

@GrpcHandler
class TeamHandler(
    private val teamService: TeamService,
) : TeamHandlerGrpcKt.TeamHandlerCoroutineImplBase() {

    override suspend fun createTeam(request: CreateTeamRequest): TeamResponse {
        return teamService.createTeam(TeamMapper.generateCreateTeamRequest(request))
            .let { TeamMapper.generateTeamResponse(it) }
    }
}
