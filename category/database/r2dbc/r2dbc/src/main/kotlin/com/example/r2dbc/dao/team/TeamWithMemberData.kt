package com.example.r2dbc.dao.team

import java.time.LocalDateTime

data class TeamWithMemberData(
    val memberId: Long,
    val memberName: String,
    val memberIntroduction: String?,
    val memberType: String,
    val memberRegisteredBy: String,
    val memberRegisteredDate: LocalDateTime,
    val memberModifiedBy: String,
    val memberModifiedDate: LocalDateTime,
    val teamId: Long,
    val teamName: String,
    val teamRegisteredBy: String,
    val teamRegisteredDate: LocalDateTime,
    val teamModifiedBy: String,
    val teamModifiedDate: LocalDateTime,
)
