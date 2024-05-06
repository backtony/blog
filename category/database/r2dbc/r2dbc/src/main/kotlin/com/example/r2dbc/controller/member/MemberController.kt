package com.example.r2dbc.controller.member

import com.example.r2dbc.controller.member.dto.MemberDto
import com.example.r2dbc.controller.member.dto.MemberDto.CreateMemberRequest
import com.example.r2dbc.controller.member.dto.MemberDto.MemberResponse
import com.example.r2dbc.controller.member.dto.MemberDto.SearchCondition
import com.example.r2dbc.service.member.MemberService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberController(
    private val memberService: MemberService,
) {

    @PostMapping("/members")
    suspend fun createMember(@RequestBody request: CreateMemberRequest): MemberResponse {
        return memberService.createMember(request)
            .let { MemberResponse.from(it) }
    }

    @GetMapping("/members/{id}")
    suspend fun getMember(@PathVariable id: Long): MemberResponse {
        return memberService.getMember(id)
            .let { MemberResponse.from(it) }
    }

    @PatchMapping("/members/{id}")
    suspend fun updateMember(
        @PathVariable id: Long,
        @RequestBody updateMemberRequest: MemberDto.UpdateMemberRequest,
    ): MemberResponse {
        return memberService.updateMember(id, updateMemberRequest)
            .let { MemberResponse.from(it) }
    }

    @PostMapping("/members/search")
    suspend fun searchMember(@RequestBody searchCondition: SearchCondition): MemberDto.MemberSearchResponse {
        return MemberDto.MemberSearchResponse.of(
            members = memberService.search(searchCondition).map { MemberResponse.from(it) },
            totalCount = memberService.searchCount(searchCondition),
            size = searchCondition.size
        )
    }
}
