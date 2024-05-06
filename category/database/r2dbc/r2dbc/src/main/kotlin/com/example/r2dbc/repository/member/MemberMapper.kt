package com.example.r2dbc.repository.member

import com.example.r2dbc.dao.member.MemberWithTeam
import com.example.r2dbc.domain.Member
import com.example.r2dbc.domain.Team
import com.example.r2dbc.repository.team.TeamMapper

object MemberMapper {

    fun mapToDao(member: Member): com.example.r2dbc.dao.member.Member {
        return with(member) {
            com.example.r2dbc.dao.member.Member(
                id = id,
                name = name,
                introduction = introduction,
                type = type.name,
                teamId = teamId,
                registeredBy = registeredBy,
                registeredDate = registeredDate,
                modifiedBy = modifiedBy,
                modifiedDate = modifiedDate
            )
        }
    }

    fun mapToDomain(member: com.example.r2dbc.dao.member.Member, getTeam: suspend () -> Team?): Member {
        return with(member) {
            Member(
                id = id,
                name = name,
                introduction = introduction,
                type = Member.Type.valueOf(type),
                teamId = teamId,
                registeredBy = registeredBy,
                registeredDate = registeredDate,
                modifiedBy = modifiedBy,
                modifiedDate = modifiedDate,
                teamProvider = getTeam
            )
        }
    }

    fun mapToDomain(memberWithTeam: MemberWithTeam): Member {
        return with(memberWithTeam) {
            Member(
                id = memberId,
                name = memberName,
                introduction = memberIntroduction,
                type = Member.Type.valueOf(memberType),
                teamId = teamId,
                registeredBy = memberRegisteredBy,
                registeredDate = memberRegisteredDate,
                modifiedBy = memberModifiedBy,
                modifiedDate = memberModifiedDate,
                teamProvider = { TeamMapper.mapToDomain(memberWithTeam) }
            )
        }
    }
}
