package com.example.r2dbc.dao.team

import com.example.r2dbc.dao.member.Member

data class TeamWithMembers(
    val team: Team,
    val members: List<Member>,
)
