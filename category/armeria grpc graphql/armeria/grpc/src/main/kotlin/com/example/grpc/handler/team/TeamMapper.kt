package com.example.grpc.handler.team

import com.example.grpc.domain.Team
import com.example.grpc.service.team.dto.TeamDto
import com.example.grpc.utils.toTimestamp
import com.example.proto.team.CreateTeamRequest
import com.example.proto.team.TeamResponse
import com.example.proto.team.teamResponse

object TeamMapper {

    fun generateCreateTeamRequest(request: CreateTeamRequest): TeamDto.CreateTeamRequest {

        return with(request) {
            TeamDto.CreateTeamRequest(
                name = name,
                requestedBy = requestedBy,
            )
        }
    }

    fun generateTeamResponse(team: Team): TeamResponse {

        return teamResponse {
            id = team.id!!
            name = team.name
            registeredBy = team.registeredBy
            registeredDate = team.registeredDate.toTimestamp()
            modifiedBy = team.modifiedBy
            modifiedDate = team.modifiedDate.toTimestamp()
        }
    }
}
