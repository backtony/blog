package com.example.grpc.handler.member

import com.example.grpc.domain.Member
import com.example.grpc.service.member.dto.MemberDto
import com.example.grpc.utils.toTimestamp
import com.example.proto.member.Country
import com.example.proto.member.CreateMemberRequest
import com.example.proto.member.MemberListResponse
import com.example.proto.member.MemberResponse
import com.example.proto.member.Type
import com.example.proto.member.introductionOrNull
import com.example.proto.member.memberListResponse
import com.example.proto.member.memberResponse
import com.example.proto.member.teamIdOrNull
import com.google.protobuf.Int64Value
import com.google.protobuf.StringValue

object MemberMapper {

    fun generateCreateMemberRequest(request: CreateMemberRequest): MemberDto.CreateMemberRequest {

        return with(request) {
            MemberDto.CreateMemberRequest(
                name = name,
                introduction = introductionOrNull?.value,
                type = Member.Type.valueOf(type.name),
                country = Member.Country.valueOf(country.name),
                teamId = teamIdOrNull?.value,
                requestedBy = requestedBy,
            )
        }
    }

    fun generateMemberResponse(member: Member): MemberResponse {

        return memberResponse {
            id = member.id!!
            name = member.name
            member.introduction?.let {
                introduction = StringValue.of(it)
            }
            type = Type.valueOf(member.type.name)
            country = Country.valueOf(member.country.name)
            member.teamId?.let {
                teamId = Int64Value.of(it)
            }
            registeredBy = member.registeredBy
            registeredDate = member.registeredDate.toTimestamp()
            modifiedBy = member.modifiedBy
            modifiedDate = member.modifiedDate.toTimestamp()
        }
    }

    fun generateMemberListResponse(members: List<Member>): MemberListResponse {
        return memberListResponse {
            member.addAll(members.map { generateMemberResponse(it) })
        }
    }
}
