package com.example.grpc.handler.member

import com.example.grpc.config.armeria.GrpcHandler
import com.example.grpc.service.member.MemberService
import com.example.proto.common.IdRequest
import com.example.proto.common.IdsRequest
import com.example.proto.member.CreateMemberRequest
import com.example.proto.member.MemberHandlerGrpcKt
import com.example.proto.member.MemberListResponse
import com.example.proto.member.MemberResponse

@GrpcHandler
class MemberHandler(
    private val memberService: MemberService
) : MemberHandlerGrpcKt.MemberHandlerCoroutineImplBase() {

    override suspend fun createMember(request: CreateMemberRequest): MemberResponse {
        return memberService.createMember(MemberMapper.generateCreateMemberRequest(request))
            .let { MemberMapper.generateMemberResponse(it) }
    }

    override suspend fun getMembersByTeamIds(request: IdsRequest): MemberListResponse {
        return memberService.getMembersByTeamIds(request.idList)
            .let { MemberMapper.generateMemberListResponse(it) }
    }

    override suspend fun getMember(request: IdRequest): MemberResponse {
        return memberService.getMember(request.id)
            .let { MemberMapper.generateMemberResponse(it) }
    }
}
