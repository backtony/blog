package com.example.springgraphql.service.member

import com.example.springgraphql.client.member.MemberClient
import com.example.springgraphql.model.CreateMemberInput
import com.example.springgraphql.model.Member
import org.springframework.stereotype.Controller

@Controller
class MemberService(
    private val memberClient: MemberClient,
) {

    suspend fun createMember(input: CreateMemberInput): Member {
        return memberClient.createMember(input)
    }

    suspend fun getMember(id: Long): Member {
        return memberClient.getMember(id)
    }

    suspend fun getMembersByTeamIds(teamIds: List<Long>): List<Member> {
        return memberClient.getMembersByTeamIds(teamIds)
    }
}
