package com.example.grpc.handler.member

import com.example.grpc.config.armeria.GrpcHandler
import com.example.grpc.service.member.MemberService
import com.example.proto.member.CreateMemberRequest
import com.example.proto.member.MemberHandlerGrpcKt
import com.example.proto.member.MemberListResponse
import com.example.proto.member.MemberResponse
import com.example.proto.member.TeamId

@GrpcHandler
class MemberHandler(
    private val memberService: MemberService
) : MemberHandlerGrpcKt.MemberHandlerCoroutineImplBase() {

    override suspend fun createMember(request: CreateMemberRequest): MemberResponse {
        return memberService.createMember(MemberMapper.generateCreateMemberRequest(request))
            .let { MemberMapper.generateMemberResponse(it) }
    }

    override suspend fun getMembersByTeamId(request: TeamId): MemberListResponse {
        return memberService.getMembersByTeamId(request.id)
            .let { MemberMapper.generateMemberListResponse(it) }
    }
}
