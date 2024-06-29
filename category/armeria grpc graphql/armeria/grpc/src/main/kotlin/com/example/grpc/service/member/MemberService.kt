package com.example.grpc.service.member

import com.example.event.member.MemberCreatedEvent
import com.example.grpc.domain.Member
import com.example.grpc.repository.MemberRepository
import com.example.grpc.service.event.TransactionalDomainEventPublisher
import com.example.grpc.service.member.dto.MemberDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MemberService(
    private val memberRepository: MemberRepository,
    private val producer: TransactionalDomainEventPublisher,
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
            .also { producer.publishEvent(MemberCreatedEvent(it.id!!)) }
    }

    @Transactional(readOnly = true)
    suspend fun getMember(id: Long): Member {
        return memberRepository.findById(id) ?: throw RuntimeException("member not found by id. id : $id")
    }

    @Transactional(readOnly = true)
    suspend fun getMembersByTeamIds(idList: List<Long>): List<Member> {
        return memberRepository.findAllByTeamIdIn(idList)
    }
}
