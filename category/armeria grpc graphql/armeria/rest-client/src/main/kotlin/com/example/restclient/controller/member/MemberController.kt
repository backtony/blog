package com.example.restclient.controller.member

import com.example.restclient.controller.member.dto.MemberDto
import com.example.restclient.service.member.MemberService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class MemberController(
    private val memberService: MemberService,
) {

    @PostMapping("/members")
    suspend fun createMember(@RequestBody request: MemberDto.CreateMemberRequest): MemberDto.MemberResponse {
        return memberService.createMember(request)
    }
}
