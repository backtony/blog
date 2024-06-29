package com.example.springgraphql.controller.member

import com.example.springgraphql.api.MemberMutationResolver
import com.example.springgraphql.api.MemberQueryResolver
import com.example.springgraphql.model.CreateMemberInput
import com.example.springgraphql.model.Member
import com.example.springgraphql.model.Team
import com.example.springgraphql.service.member.MemberService
import com.example.springgraphql.service.team.TeamService
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.data.method.annotation.Argument
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller

@Controller
class MemberController(
    private val memberService: MemberService,
    private val teamService: TeamService,
) : MemberQueryResolver, MemberMutationResolver {

    override suspend fun member(id: Long, env: DataFetchingEnvironment): Member? {
        return memberService.getMember(id)
    }

    @BatchMapping
    suspend fun team(@Argument members: List<Member>): Map<Member, Team?> {
        val teams = teamService.getTeams(members.mapNotNull { it.teamId })
        return members.associateWith { member ->
            teams.firstOrNull { it.id == member.teamId }
        }
    }

    override suspend fun createMember(input: CreateMemberInput, env: DataFetchingEnvironment): Member {
        return memberService.createMember(input)
    }
}
