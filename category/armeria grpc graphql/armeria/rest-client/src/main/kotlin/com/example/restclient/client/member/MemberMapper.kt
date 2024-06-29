package com.example.restclient.client.member

import com.example.proto.member.Country
import com.example.proto.member.CreateMemberRequest
import com.example.proto.member.MemberListResponse
import com.example.proto.member.MemberResponse
import com.example.proto.member.Type
import com.example.proto.member.createMemberRequest
import com.example.proto.member.introductionOrNull
import com.example.proto.member.teamIdOrNull
import com.example.restclient.controller.member.dto.MemberDto
import com.example.restclient.utils.toLocalDateTime
import com.google.protobuf.Int64Value
import com.google.protobuf.StringValue

object MemberMapper {

    fun generateCreateMemberRequest(request: MemberDto.CreateMemberRequest): CreateMemberRequest {
        return createMemberRequest {
            name = request.name
            request.introduction?.let {
                introduction = StringValue.of(it)
            }
            country = Country.valueOf(request.country.name)
            type = Type.valueOf(request.type.name)
            request.teamId?.let {
                teamId = Int64Value.of(it)
            }
            requestedBy = request.requestedBy
        }
    }

    fun generateMemberResponse(response: MemberResponse): MemberDto.MemberResponse {
        return MemberDto.MemberResponse(
            id = response.id,
            name = response.name,
            introduction = response.introductionOrNull?.value,
            type = MemberDto.Type.valueOf(response.type.name),
            country = MemberDto.Country.valueOf(response.country.name),
            teamId = response.teamIdOrNull?.value,
            registeredBy = response.registeredBy,
            registeredDate = response.registeredDate.toLocalDateTime(),
            modifiedBy = response.modifiedBy,
            modifiedDate = response.modifiedDate.toLocalDateTime(),
        )
    }

    fun generateMemberListResponse(response: MemberListResponse): MemberDto.MemberListResponse {
        return MemberDto.MemberListResponse(
            members = response.memberList.map { generateMemberResponse(it) },
        )
    }
}
