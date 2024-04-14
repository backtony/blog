package com.example.grpc.service.member.dto

import com.example.grpc.domain.Member

object MemberDto {

    data class CreateMemberRequest(
        val name: String,
        val introduction: String?,
        val type: Member.Type,
        val country: Member.Country,
        val teamId: Long?,
        val requestedBy: String,
    )
}
