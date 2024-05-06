package com.example.r2dbc.controller.team

import com.example.r2dbc.controller.team.dto.TeamDto.CreateTeamRequest
import com.example.r2dbc.controller.team.dto.TeamDto.TeamResponse
import com.example.r2dbc.controller.team.dto.TeamDto.TeamWithMembersListResponse
import com.example.r2dbc.service.team.TeamService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TeamController(
    private val teamService: TeamService,
) {

    @PostMapping("/teams")
    suspend fun createTeam(
        @RequestBody request: CreateTeamRequest,
    ): TeamResponse {
        return teamService.createTeam(request)
            .let { TeamResponse.from(it) }
    }

    @GetMapping("/teams")
    suspend fun getTeamsWithMembers(): TeamWithMembersListResponse {
        return teamService.getTeamsWithMembers()
            .let { TeamWithMembersListResponse.from(it) }
    }
}
