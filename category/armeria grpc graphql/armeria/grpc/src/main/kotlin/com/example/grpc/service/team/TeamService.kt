package com.example.grpc.service.team

import com.example.grpc.domain.Team
import com.example.grpc.repository.MemberRepository
import com.example.grpc.repository.TeamRepository
import com.example.grpc.service.team.dto.TeamDto
import com.example.proto.team.TeamListResponse
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TeamService(
    private val teamRepository: TeamRepository, private val memberRepository: MemberRepository
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

    @Transactional(readOnly = true)
    suspend fun getTeam(id: Long): Team {
        return teamRepository.findById(id) ?: throw RuntimeException("team not found by id. id : $id")
    }

    @Transactional(readOnly = true)
    suspend fun getTeams(ids: List<Long>): List<Team> {
        return teamRepository.findAllById(ids).toList()
    }
}
