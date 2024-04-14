package com.example.restclient.controller.team

import com.example.restclient.controller.team.dto.TeamDto
import com.example.restclient.service.team.TeamService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class TeamController(
    private val teamService: TeamService,
) {

    @PostMapping("/teams")
    suspend fun createTeam(@RequestBody request: TeamDto.CreateTeamRequest): TeamDto.TeamResponse {
        return teamService.createTeam(request)
    }
}
