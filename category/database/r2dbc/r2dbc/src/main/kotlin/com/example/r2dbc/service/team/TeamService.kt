package com.example.r2dbc.service.team

import com.example.r2dbc.controller.team.dto.TeamDto
import com.example.r2dbc.domain.Member
import com.example.r2dbc.domain.Team
import com.example.r2dbc.repository.team.TeamRootRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeamService(
    private val teamRootRepository: TeamRootRepository,
) {

    @Transactional
    suspend fun createTeam(request: TeamDto.CreateTeamRequest): Team {
        return teamRootRepository.save(
            with(request) {
                Team(
                    name = name,
                    registeredBy = requestedBy,
                    modifiedBy = requestedBy
                )
            }
        )
    }

    @Transactional(readOnly = true)
    suspend fun getTeamsWithMembers(): Map<Team, List<Member>> {
        return teamRootRepository.findTeamWithMembers()

    }
}
