package com.example.r2dbc.repository.member

import com.example.r2dbc.controller.member.dto.MemberDto
import com.example.r2dbc.domain.Member
import com.example.r2dbc.repository.team.TeamMapper
import com.example.r2dbc.repository.team.TeamRootRepository
import org.springframework.stereotype.Repository

@Repository
class MemberRootRepository(
    private val memberRepository: MemberRepository,
    private val teamRootRepository: TeamRootRepository,
) {

    suspend fun save(member: Member): Member {
        return memberRepository.save(MemberMapper.mapToDao(member)).let {
            MemberMapper.mapToDomain(it) { it.teamId?.let { teamRootRepository.findById(it) } }
        }
    }

    suspend fun findById(id: Long): Member? {
        return memberRepository.findById(id)?.let {
            MemberMapper.mapToDomain(it) { it.teamId?.let { teamRootRepository.findById(it) } }
        }
    }

    suspend fun findByIdFetch(id: Long): Member? {
        return memberRepository.findByIdFetch(id)
            ?.let { MemberMapper.mapToDomain(it) }
    }

    suspend fun search(searchCondition: MemberDto.SearchCondition): List<Member> {
        return memberRepository.search(searchCondition).map { MemberMapper.mapToDomain(it) }
    }

    suspend fun searchCount(searchCondition: MemberDto.SearchCondition): Long {
        return memberRepository.searchCount(searchCondition)
    }

    suspend fun deleteById(id: Long) {
        memberRepository.deleteById(id)
    }
}
