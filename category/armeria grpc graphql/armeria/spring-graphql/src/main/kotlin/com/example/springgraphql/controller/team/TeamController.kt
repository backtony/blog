package com.example.springgraphql.controller.team

import com.example.springgraphql.api.TeamMutationResolver
import com.example.springgraphql.api.TeamQueryResolver
import com.example.springgraphql.model.CreateTeamInput
import com.example.springgraphql.model.Member
import com.example.springgraphql.model.Team
import com.example.springgraphql.service.member.MemberService
import com.example.springgraphql.service.team.TeamService
import graphql.schema.DataFetchingEnvironment
import org.springframework.graphql.data.method.annotation.BatchMapping
import org.springframework.stereotype.Controller

@Controller
class TeamController(
    private val teamService: TeamService,
    private val memberService: MemberService,
) : TeamQueryResolver, TeamMutationResolver {


    override suspend fun createTeam(input: CreateTeamInput, env: DataFetchingEnvironment): Team {
        return teamService.createTeam(input)
    }

    override suspend fun team(id: Long, env: DataFetchingEnvironment): Team? {
        return teamService.getTeam(id)
    }

    override suspend fun teams(ids: List<Long>, env: DataFetchingEnvironment): List<Team> {
        return teamService.getTeams(ids)
    }

    @BatchMapping
    suspend fun members(teams: List<Team>): Map<Team, List<Member>> {
        val members = memberService.getMembersByTeamIds(teams.map { it.id })
        return teams.associateWith { team ->
            members.filter { it.teamId == team.id }
        }
    }
}
