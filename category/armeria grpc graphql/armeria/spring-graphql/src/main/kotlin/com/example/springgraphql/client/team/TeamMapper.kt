package com.example.springgraphql.client.team

import com.example.proto.team.CreateTeamRequest
import com.example.proto.team.TeamResponse
import com.example.proto.team.createTeamRequest
import com.example.springgraphql.model.CreateTeamInput
import com.example.springgraphql.model.Team
import com.example.springgraphql.utils.toLocalDateTime

object TeamMapper {

    fun generateCreateTeamRequest(input: CreateTeamInput): CreateTeamRequest {
        return createTeamRequest {
            name = input.name
            requestedBy = input.requestedBy
        }
    }

    fun generateTeam(response: TeamResponse): Team {
        return Team(
            id = response.id,
            name = response.name,
            registeredBy = response.registeredBy,
            registeredDate = response.registeredDate.toLocalDateTime(),
            modifiedBy = response.modifiedBy,
            modifiedDate = response.modifiedDate.toLocalDateTime(),
            members = emptyList(),
        )
    }
}
