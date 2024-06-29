package com.example.grpc.handler.team

import com.example.grpc.config.armeria.GrpcHandler
import com.example.grpc.service.team.TeamService
import com.example.proto.common.IdRequest
import com.example.proto.common.IdsRequest
import com.example.proto.team.CreateTeamRequest
import com.example.proto.team.TeamHandlerGrpcKt
import com.example.proto.team.TeamListResponse
import com.example.proto.team.TeamResponse

@GrpcHandler
class TeamHandler(
    private val teamService: TeamService,
) : TeamHandlerGrpcKt.TeamHandlerCoroutineImplBase() {

    override suspend fun createTeam(request: CreateTeamRequest): TeamResponse {
        return teamService.createTeam(TeamMapper.generateCreateTeamRequest(request))
            .let { TeamMapper.generateTeamResponse(it) }
    }

    override suspend fun getTeam(request: IdRequest): TeamResponse {
        return teamService.getTeam(request.id)
            .let { TeamMapper.generateTeamResponse(it) }
    }

    override suspend fun getTeams(request: IdsRequest): TeamListResponse {
        return teamService.getTeams(request.idList)
            .let { TeamMapper.generateTeamListResponse(it) }
    }
}
