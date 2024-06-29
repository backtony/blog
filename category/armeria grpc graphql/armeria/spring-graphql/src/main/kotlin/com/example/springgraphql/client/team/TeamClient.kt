package com.example.springgraphql.client.team

import com.example.proto.team.TeamHandlerGrpcKt
import com.example.springgraphql.client.common.CommonMapper
import com.example.springgraphql.model.CreateTeamInput
import com.example.springgraphql.model.Team
import org.springframework.stereotype.Component

@Component
class TeamClient(
    private val teamServiceStub: TeamHandlerGrpcKt.TeamHandlerCoroutineStub,
) {
    suspend fun createTeam(input: CreateTeamInput): Team {
        return teamServiceStub.createTeam(TeamMapper.generateCreateTeamRequest(input))
            .let { TeamMapper.generateTeam(it) }
    }

    suspend fun getTeam(id: Long): Team {
        return teamServiceStub.getTeam(CommonMapper.generateIdRequest(id))
            .let { TeamMapper.generateTeam(it) }
    }

    suspend fun getTeams(teamIds: List<Long>): List<Team> {
        return teamServiceStub.getTeams(CommonMapper.generateIdsRequest(teamIds))
            .teamList.map { TeamMapper.generateTeam(it) }

    }
}
