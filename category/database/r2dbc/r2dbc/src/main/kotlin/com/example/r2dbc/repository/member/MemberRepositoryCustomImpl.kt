package com.example.r2dbc.repository.member

import com.example.r2dbc.controller.member.dto.MemberDto
import com.example.r2dbc.dao.member.Member
import com.example.r2dbc.dao.member.MemberWithTeam
import com.example.r2dbc.utils.bindConditions
import com.example.r2dbc.utils.bindPage
import com.example.r2dbc.utils.plusOrderBy
import com.example.r2dbc.utils.plusPagination
import com.example.r2dbc.utils.plusWhere
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.convert.MappingR2dbcConverter
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.core.select
import org.springframework.data.relational.core.query.Criteria
import org.springframework.data.relational.core.query.Query
import org.springframework.stereotype.Repository


@Repository
class MemberRepositoryCustomImpl(
    private val template: R2dbcEntityTemplate,
    private val converter: MappingR2dbcConverter
): MemberRepositoryCustom {

    // https://docs.spring.io/spring-data/r2dbc/docs/current-SNAPSHOT/reference/html/#r2dbc.entityoperations
    override suspend fun findById(id: Long): Member? {
        val sql = template.select<Member>()
        val whereBuilder = Criteria.where("id").`is`(id)

        return sql
            .matching(Query.query(whereBuilder))
            .one()
            .awaitSingleOrNull()
    }

    // https://docs.spring.io/spring-data/r2dbc/docs/current-SNAPSHOT/reference/html/#r2dbc.getting-started
    // https://docs.spring.io/spring-data/relational/reference/r2dbc/mapping.html
    override suspend fun findByIdFetch(id: Long): MemberWithTeam? {

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
            FROM member m 
            LEFT JOIN team t on m.team_id = t.id
            WHERE m.id =:memberId
        """.trimIndent()

        return template.databaseClient
            .sql(sql)
            .bind("memberId", id)
            .map { row, metaData -> converter.read(MemberWithTeam::class.java, row, metaData) }
            .one()
            .awaitSingleOrNull()
    }

    override suspend fun search(searchCondition: MemberDto.SearchCondition): List<MemberWithTeam> {
        val baseSql = """
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
            FROM member m 
            LEFT JOIN team t on m.team_id = t.id
        """.trimIndent()

        val whereConditions = generateWhereCondition(searchCondition)

        return template.databaseClient
            .sql(
                baseSql
                    .plusWhere(whereConditions)
                    .plusOrderBy(searchCondition.sort.map { Pair("m.${it.field}", Sort.Direction.valueOf(it.sort.name)) })
                    .plusPagination(searchCondition.page, searchCondition.size)
            )
            .bindConditions(whereConditions)
            .bindPage(searchCondition.page, searchCondition.size)
            .map { row, metaData -> converter.read(MemberWithTeam::class.java, row, metaData) }
            .all()
            .collectList()
            .awaitSingle()
    }

    override suspend fun searchCount(searchCondition: MemberDto.SearchCondition): Long {
        val baseSql = """
            SELECT 
                COUNT(1)
            FROM member m
        """.trimIndent()

        val whereConditions = generateWhereCondition(searchCondition)

        return template.databaseClient
            .sql(
                baseSql
                    .plusWhere(whereConditions)
                    .plusPagination(searchCondition.page, searchCondition.size)
            )
            .bindConditions(whereConditions)
            .bindPage(searchCondition.page, searchCondition.size)
            // TODO primitive 타입 지원 X
//            .map { row, metaData -> converter.read(Long::class.java, row, metaData) }
            .map { row, _ -> row.get(0, Long::class.java)!! }
            .one()
            .awaitSingle()
    }

    private fun generateWhereCondition(searchCondition: MemberDto.SearchCondition): MutableMap<String, Any?> {
        val whereConditions = mutableMapOf<String, Any?>()
        searchCondition.name?.let { whereConditions["m.name = :name"] = it }
        searchCondition.type?.let { whereConditions["m.type = :type"] = it }
        return whereConditions
    }
}
