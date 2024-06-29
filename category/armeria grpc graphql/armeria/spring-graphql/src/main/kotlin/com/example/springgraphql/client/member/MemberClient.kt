package com.example.springgraphql.client.member

import com.example.proto.member.MemberHandlerGrpcKt
import com.example.springgraphql.client.common.CommonMapper
import com.example.springgraphql.model.CreateMemberInput
import com.example.springgraphql.model.Member
import org.springframework.stereotype.Component

@Component
class MemberClient(
    private val memberServiceStub: MemberHandlerGrpcKt.MemberHandlerCoroutineStub,
) {
    suspend fun createMember(input: CreateMemberInput): Member {
        return memberServiceStub.createMember(MemberMapper.generateCreateMemberRequest(input))
            .let { MemberMapper.generateMember(it) }
    }

    suspend fun getMember(id: Long): Member {
        return memberServiceStub.getMember(CommonMapper.generateIdRequest(id))
            .let { MemberMapper.generateMember(it) }
    }

    suspend fun getMembersByTeamIds(teamIds: List<Long>): List<Member> {
        return memberServiceStub.getMembersByTeamIds(CommonMapper.generateIdsRequest(teamIds))
            .memberList.map { MemberMapper.generateMember(it) }
    }
}
