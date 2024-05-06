package com.example.r2dbc.controller.team.dto

import com.example.r2dbc.controller.member.dto.MemberDto
import com.example.r2dbc.domain.Member
import com.example.r2dbc.domain.Team
import java.time.LocalDateTime

object TeamDto {

    data class CreateTeamRequest(
        val name: String,
        val requestedBy: String,
    )

    data class TeamResponse(
        val id: Long,
        val name: String,
        val registeredBy: String,
        val registeredDate: LocalDateTime,
        val modifiedBy: String,
        val modifiedDate: LocalDateTime,
    ) {
        companion object {
            fun from(team: Team): TeamResponse {

                return TeamResponse(
                    id = team.id!!,
                    name = team.name,
                    registeredBy = team.registeredBy,
                    registeredDate = team.registeredDate,
                    modifiedBy = team.modifiedBy,
                    modifiedDate = team.modifiedDate,
                )
            }
        }
    }

    data class TeamWithMembersResponse(
        val team: TeamResponse,
        val members: List<MemberDto.MemberResponse>,
    )

    data class TeamWithMembersListResponse(
        val teamWithMembers: List<TeamWithMembersResponse>,
    ) {
        companion object {
            suspend fun from(teamWithMembers: Map<Team, List<Member>>): TeamWithMembersListResponse {

                return TeamWithMembersListResponse(
                    teamWithMembers.map {
                        TeamWithMembersResponse(
                            team = TeamResponse.from(it.key),
                            members = it.value.map { MemberDto.MemberResponse.from(it) }
                        )
                    }
                )
            }
        }
    }
}
