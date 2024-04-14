package com.example.grpc.service.team

import com.example.grpc.domain.Team
import com.example.grpc.repository.TeamRepository
import com.example.grpc.service.team.dto.TeamDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeamService(
    private val teamRepository: TeamRepository
) {

    @Transactional
    suspend fun createTeam(request: TeamDto.CreateTeamRequest): Team {
        return teamRepository.save(
            with(request) {
                Team(
                    name = name,
                    registeredBy = requestedBy,
                    modifiedBy = requestedBy
                )
            }
        )
    }
}
