package com.example.r2dbc.repository.member

import com.example.r2dbc.controller.member.dto.MemberDto.SearchCondition
import com.example.r2dbc.dao.member.Member
import org.springframework.data.repository.kotlin.CoroutineCrudRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : CoroutineCrudRepository<Member, Long>, MemberRepositoryCustom
