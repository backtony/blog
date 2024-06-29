package com.example.restclient.service.member

import com.example.restclient.client.member.MemberClient
import com.example.restclient.controller.member.dto.MemberDto
import org.springframework.stereotype.Service

@Service
class MemberService(
    private val memberClient: MemberClient,
) {

    suspend fun createMember(createMemberRequest: MemberDto.CreateMemberRequest): MemberDto.MemberResponse {
        return memberClient.createMember(createMemberRequest)
    }
}
