package com.example.r2dbc.repository.team

import com.example.r2dbc.dao.team.TeamWithMembers


interface TeamRepositoryCustom {
    suspend fun findAllTeamWithMembers(): List<TeamWithMembers>
}
