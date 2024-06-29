package com.example.restclient.client.member

import com.example.proto.member.MemberHandlerGrpcKt
import com.example.restclient.controller.member.dto.MemberDto
import com.example.springgraphql.client.common.CommonMapper
import org.springframework.stereotype.Component

@Component
class MemberClient(
    private val memberServiceStub: MemberHandlerGrpcKt.MemberHandlerCoroutineStub,
) {
    suspend fun createMember(request: MemberDto.CreateMemberRequest): MemberDto.MemberResponse {
        return memberServiceStub.createMember(MemberMapper.generateCreateMemberRequest(request))
            .let { MemberMapper.generateMemberResponse(it) }
    }
}
