package com.example.r2dbc.repository.team

import com.example.r2dbc.dao.team.TeamWithMembers
import com.example.r2dbc.domain.Member
import com.example.r2dbc.domain.Team
import com.example.r2dbc.repository.member.MemberMapper
import org.springframework.stereotype.Repository

@Repository
class TeamRootRepository(
    private val teamRepository: TeamRepository,
) {

    suspend fun save(team: Team): Team {
        return teamRepository.save(TeamMapper.mapToDao(team)).let {
            TeamMapper.mapToDomain(it)
        }
    }

    suspend fun findById(id: Long): Team? {
        return teamRepository.findById(id)?.let {
            TeamMapper.mapToDomain(it)
        }
    }

    suspend fun findTeamWithMembers(): Map<Team, List<Member>> {
        return teamRepository.findAllTeamWithMembers().associate {
            val team = TeamMapper.mapToDomain(it.team)
            team to it.members.map { MemberMapper.mapToDomain(it) { team } }
        }
    }
}
