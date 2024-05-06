package com.example.r2dbc.controller.member.dto

import com.example.r2dbc.controller.team.dto.TeamDto
import com.example.r2dbc.domain.Member
import org.springframework.data.domain.Sort.Direction
import java.time.LocalDateTime
import kotlin.math.ceil

object MemberDto {

    data class CreateMemberRequest(
        val name: String,
        val introduction: String?,
        val type: String,
        val teamId: Long?,
        val requestedBy: String,
    )

    data class UpdateMemberRequest(
        val introduction: String?,
        val type: String,
        val teamId: Long?,
        val requestedBy: String,
    )

    data class MemberResponse(
        val id: Long,
        val name: String,
        val introduction: String?,
        val type: String,
        val team: TeamDto.TeamResponse?,
        val registeredBy: String,
        val registeredDate: LocalDateTime,
        val modifiedBy: String,
        val modifiedDate: LocalDateTime,
    ) {
        companion object {
            suspend fun from(member: Member): MemberResponse {
                return MemberResponse(
                    id = member.id!!,
                    name = member.name,
                    introduction = member.introduction,
                    type = member.type.name,
                    registeredBy = member.registeredBy,
                    registeredDate = member.registeredDate,
                    modifiedBy = member.modifiedBy,
                    modifiedDate = member.modifiedDate,
                    team = member.getTeam()?.let { TeamDto.TeamResponse.from(it) }
                )
            }
        }
    }

    data class SearchCondition(
        val page: Int = 1,
        val size: Int = 10,
        val name: String? = null,
        val type: String? = null,
        val sort: List<Sort> = emptyList(),
    ) {

        data class Sort(
            val field: String,
            val sort: Direction,
        ) {
            enum class Direction {
                ASC, DESC
            }
        }
    }

    data class MemberSearchResponse(
        val members: List<MemberResponse>,
        val totalPage: Int,
    ) {
        companion object {
            fun of (members: List<MemberResponse>, totalCount: Long, size: Int): MemberSearchResponse {
                return MemberSearchResponse(
                    members = members,
                    totalPage = ceil(totalCount.toDouble() / size).toInt(),
                )
            }
        }
    }

    data class MemberListResponse(
        val members: List<MemberResponse>,
    ) {
        companion object {
            fun from(members: List<MemberResponse>): MemberListResponse {
                return MemberListResponse(members)
            }
        }
    }
}
