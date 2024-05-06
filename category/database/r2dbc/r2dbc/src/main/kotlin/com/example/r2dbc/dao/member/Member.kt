package com.example.r2dbc.dao.member

import com.example.r2dbc.dao.team.TeamWithMemberData
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("member")
data class Member(
    @Id
    val id: Long? = null,
    val name: String,
    val introduction: String?,
    val type: String,
    val teamId: Long?,
    val registeredBy: String,
    @CreatedDate
    val registeredDate: LocalDateTime,
    val modifiedBy: String,
    @LastModifiedDate
    val modifiedDate: LocalDateTime,
) {

    companion object {
        fun from(teamWithMemberData: TeamWithMemberData): Member {
            return with(teamWithMemberData) {
                Member(
                    id = memberId,
                    name = memberName,
                    introduction = memberIntroduction,
                    type = memberType,
                    teamId = teamId,
                    registeredBy = memberRegisteredBy,
                    registeredDate = memberRegisteredDate,
                    modifiedDate = memberModifiedDate,
                    modifiedBy = memberModifiedBy
                )
            }
        }
    }
}
