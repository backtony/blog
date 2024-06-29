package com.example.grpc.repository

import com.example.grpc.domain.Member
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : CoroutineCrudRepository<Member, Long> {

    suspend fun findAllByTeamId(teamId: Long): List<Member>

    suspend fun findAllByTeamIdIn(teamIds: List<Long>): List<Member>
}
