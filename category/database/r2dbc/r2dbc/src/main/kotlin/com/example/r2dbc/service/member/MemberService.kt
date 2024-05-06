package com.example.r2dbc.service.member

import com.example.r2dbc.controller.member.dto.MemberDto
import com.example.r2dbc.controller.member.dto.MemberDto.CreateMemberRequest
import com.example.r2dbc.controller.member.dto.MemberDto.SearchCondition
import com.example.r2dbc.domain.Member
import com.example.r2dbc.repository.member.MemberRootRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRootRepository: MemberRootRepository,
) {

    @Transactional
    suspend fun createMember(request: CreateMemberRequest): Member {

        val member = with(request) {
            Member(
                name = name,
                introduction = introduction,
                type = Member.Type.valueOf(type),
                registeredBy = requestedBy,
                modifiedBy = requestedBy,
                teamId = teamId
            )
        }

        return memberRootRepository.save(member)
    }

    @Transactional(readOnly = true)
    suspend fun getMember(memberId: Long): Member {
        return memberRootRepository.findByIdFetch(memberId)
            ?: throw RuntimeException("member not found by id : $memberId")
    }

    @Transactional(readOnly = true)
    suspend fun search(searchCondition: SearchCondition): List<Member> {
        return memberRootRepository.search(searchCondition)
    }

    @Transactional(readOnly = true)
    suspend fun searchCount(searchCondition: SearchCondition): Long {
        return memberRootRepository.searchCount(searchCondition)
    }

    @Transactional
    suspend fun updateMember(memberId: Long, request: MemberDto.UpdateMemberRequest): Member {
        val member = memberRootRepository.findById(memberId)
            ?: throw RuntimeException("member not found by id : $memberId")
        member.update(request.teamId, request.introduction, request.requestedBy)
        return memberRootRepository.save(member)
    }

    @Transactional
    suspend fun deleteMemberById(memberId: Long) {
        memberRootRepository.deleteById(memberId)
    }

//    @Transactional(readOnly = true)
//    suspend fun getMembersByTeamId(teamId: Long): List<Member> {
//        return memberDao.findAllByTeamId(teamId)
//    }
}
