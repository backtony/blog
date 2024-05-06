package com.example.r2dbc.repository.team

import com.example.r2dbc.dao.member.MemberWithTeam
import com.example.r2dbc.domain.Team

object TeamMapper {

    fun mapToDao(team: Team): com.example.r2dbc.dao.team.Team {
        return with(team) {
            com.example.r2dbc.dao.team.Team(
                id = id,
                name = name,
                registeredBy = registeredBy,
                registeredDate = registeredDate,
                modifiedBy = modifiedBy,
                modifiedDate = modifiedDate
            )
        }
    }

    fun mapToDomain(team : com.example.r2dbc.dao.team.Team) : Team {
        return with(team) {
            Team(
                id = id,
                name = name,
                registeredBy = registeredBy,
                registeredDate = registeredDate,
                modifiedBy = modifiedBy,
                modifiedDate = modifiedDate
            )
        }
    }

    fun mapToDomain(memberWithTeam: MemberWithTeam) : Team? {
        if (memberWithTeam.teamId == null) {
            return null
        }

        return with(memberWithTeam) {
            Team(
                id = teamId,
                name = teamName!!,
                registeredBy = teamRegisteredBy!!,
                registeredDate = teamRegisteredDate!!,
                modifiedBy = teamModifiedBy!!,
                modifiedDate = teamModifiedDate!!
            )
        }
    }
}
