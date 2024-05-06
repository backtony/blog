package com.example.r2dbc.repository.team

import com.example.r2dbc.dao.team.Team
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TeamRepository : CoroutineCrudRepository<Team, Long>, TeamRepositoryCustom
