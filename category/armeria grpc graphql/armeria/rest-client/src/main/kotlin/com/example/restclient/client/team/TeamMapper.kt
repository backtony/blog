package com.example.restclient.client.team

import com.example.proto.team.CreateTeamRequest
import com.example.proto.team.TeamResponse
import com.example.proto.team.createTeamRequest
import com.example.restclient.controller.team.dto.TeamDto
import com.example.restclient.utils.toLocalDateTime

object TeamMapper {

    fun generateCreateTeamRequest(request: TeamDto.CreateTeamRequest): CreateTeamRequest {
        return createTeamRequest {
            name = request.name
            requestedBy = request.requestedBy
        }
    }

    fun generateTeamResponse(response: TeamResponse): TeamDto.TeamResponse {
        return TeamDto.TeamResponse(
            id = response.id,
            name = response.name,
            registeredBy = response.registeredBy,
            registeredDate = response.registeredDate.toLocalDateTime(),
            modifiedBy = response.modifiedBy,
            modifiedDate = response.modifiedDate.toLocalDateTime(),
        )
    }
}
