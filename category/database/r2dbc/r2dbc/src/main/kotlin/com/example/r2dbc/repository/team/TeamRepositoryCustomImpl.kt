package com.example.r2dbc.repository.team

import com.example.r2dbc.dao.member.Member
import com.example.r2dbc.dao.team.Team
import com.example.r2dbc.dao.team.TeamWithMemberData
import com.example.r2dbc.dao.team.TeamWithMembers
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.stereotype.Repository

@Repository
class TeamRepositoryCustomImpl(
    private val template: R2dbcEntityTemplate,
    private val converter: MappingR2dbcConverter,
) : TeamRepositoryCustom {

    override suspend fun findAllTeamWithMembers(): List<TeamWithMembers> {
        val sql = """
            SELECT 
                m.id as member_id,
                m.name as member_name,
                m.introduction as member_introduction,
                m.type as member_type,
                m.team_id as team_id,
                m.registered_by as member_registered_by,
                m.registered_date as member_registered_date,
                m.modified_by as member_modified_by,
                m.modified_date as member_modified_date,            
                t.name as team_name,
                t.registered_by as team_registered_by,
                t.registered_date as team_registered_date,
                t.modified_by as team_modified_by,
                t.modified_date as team_modified_date
            FROM team t 
            JOIN member m on m.team_id = t.id
            order by m.id
        """.trimIndent()

        //
        return template.databaseClient
            .sql(sql)
            .map { row, metaData -> converter.read(TeamWithMemberData::class.java, row, metaData) }
            .all()
            .groupBy { it.teamId }
            .flatMap { groupFlux ->
                groupFlux.collectList().map { dataList ->
                    TeamWithMembers(
                        team = Team.from(dataList.first()),
                        members = dataList.map { Member.from(it) }
                    )
                }
            }
//            .bufferUntilChanged { it.teamId }
//            .map { dataList ->
//                TeamWithMembers(
//                    team = Team.from(dataList.first()),
//                    members = dataList.map { Member.from(it) }
//                )
//            }
            .collectList()
            .awaitSingle()
    }
}
