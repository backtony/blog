package com.example.springgraphql.service.team

import com.example.springgraphql.client.team.TeamClient
import com.example.springgraphql.model.CreateTeamInput
import com.example.springgraphql.model.Team
import org.springframework.stereotype.Service

@Service
class TeamService(
    private val teamClient: TeamClient
) {

    suspend fun createTeam(input: CreateTeamInput): Team {
        return teamClient.createTeam(input)
    }

    suspend fun getTeam(id: Long): Team {
        return teamClient.getTeam(id)
    }

    suspend fun getTeams(teamIds: List<Long>): List<Team> {
        return teamClient.getTeams(teamIds)
    }
}
