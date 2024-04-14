package com.example.restclient.controller.member.dto

import java.time.LocalDateTime

object MemberDto {

    data class CreateMemberRequest(
        val name: String,
        val introduction: String?,
        val type: Type,
        val country: Country,
        val teamId: Long?,
        val requestedBy: String,
    )

    data class MemberResponse(
        val id: Long,
        val name: String,
        val introduction: String?,
        val type: Type,
        val country: Country,
        val teamId: Long?,
        val registeredBy: String,
        val registeredDate: LocalDateTime,
        val modifiedBy: String,
        val modifiedDate: LocalDateTime,
    )

    data class MemberListResponse(
        val members: List<MemberResponse>
    )

    enum class Country(
        val value: String,
    ) {
        KR("Korea"),
        US("United States"),
        JP("Japan"),
    }

    enum class Type {
        INDIVIDUAL,
        COMPANY,
    }
}
