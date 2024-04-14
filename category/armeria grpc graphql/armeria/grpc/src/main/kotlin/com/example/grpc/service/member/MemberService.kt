package com.example.grpc.service.member

import com.example.grpc.domain.Member
import com.example.grpc.repository.MemberRepository
import com.example.grpc.service.member.dto.MemberDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository,
) {

    @Transactional
    suspend fun createMember(createMemberRequest: MemberDto.CreateMemberRequest): Member {

        val member = with(createMemberRequest) {
            Member(
                name = name,
                introduction = introduction,
                type = type,
                country = country,
                teamId = teamId,
                registeredBy = requestedBy,
                modifiedBy = requestedBy,
            )
        }
        return memberRepository.save(member)
    }

    @Transactional(readOnly = true)
    suspend fun getMembersByTeamId(teamId: Long): List<Member> {
        return memberRepository.findAllByTeamId(teamId)
    }
}
