package com.example.springgraphql.client.member

import com.example.proto.member.Country
import com.example.proto.member.CreateMemberRequest
import com.example.proto.member.MemberResponse
import com.example.proto.member.Type
import com.example.proto.member.createMemberRequest
import com.example.proto.member.introductionOrNull
import com.example.proto.member.teamIdOrNull
import com.example.springgraphql.model.CreateMemberInput
import com.example.springgraphql.model.Member
import com.example.springgraphql.model.MemberType
import com.example.springgraphql.utils.toLocalDateTime
import com.google.protobuf.Int64Value
import com.google.protobuf.StringValue

object MemberMapper {

    fun generateCreateMemberRequest(input: CreateMemberInput): CreateMemberRequest {
        return createMemberRequest {
            name = input.name
            input.introduction?.let {
                introduction = StringValue.of(it)
            }
            country = Country.valueOf(input.country.name)
            type = Type.valueOf(input.type.name)
            input.teamId?.let {
                teamId = Int64Value.of(it)
            }
            requestedBy = input.requestedBy
        }
    }

    fun generateMember(response: MemberResponse): Member {
        return Member(
            id = response.id,
            name = response.name,
            introduction = response.introductionOrNull?.value,
            type = MemberType.valueOf(response.type.name),
            country = com.example.springgraphql.model.Country.valueOf(response.country.name),
            teamId = response.teamIdOrNull?.value,
            team = null,
            registeredBy = response.registeredBy,
            registeredDate = response.registeredDate.toLocalDateTime(),
            modifiedBy = response.modifiedBy,
            modifiedDate = response.modifiedDate.toLocalDateTime(),
        )
    }
}
