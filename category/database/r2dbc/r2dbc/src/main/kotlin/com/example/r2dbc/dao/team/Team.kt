package com.example.r2dbc.dao.team

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("team")
class Team(
    @Id
    val id: Long? = null,
    val name: String,
    val registeredBy: String,
    @CreatedDate
    val registeredDate: LocalDateTime,
    val modifiedBy: String,
    @LastModifiedDate
    val modifiedDate: LocalDateTime,
) {
    companion object {
        fun from(teamWithMemberData: TeamWithMemberData) : Team {
            return with(teamWithMemberData) {
                Team(
                    id = teamId,
                    name = teamName,
                    registeredDate = teamRegisteredDate,
                    registeredBy = teamRegisteredBy,
                    modifiedBy = teamModifiedBy,
                    modifiedDate = teamModifiedDate,
                )
            }
        }
    }
}

