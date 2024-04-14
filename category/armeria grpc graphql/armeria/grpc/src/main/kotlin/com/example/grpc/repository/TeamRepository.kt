package com.example.grpc.repository

import com.example.grpc.domain.Team
import org.springframework.data.repository.kotlin.CoroutineCrudRepository

interface TeamRepository : CoroutineCrudRepository<Team, Long>
