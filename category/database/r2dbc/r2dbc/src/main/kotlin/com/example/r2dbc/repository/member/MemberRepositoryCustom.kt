package com.example.r2dbc.repository.member

import com.example.r2dbc.controller.member.dto.MemberDto.SearchCondition
import com.example.r2dbc.dao.member.Member
import com.example.r2dbc.dao.member.MemberWithTeam


interface MemberRepositoryCustom {

    suspend fun findById(id: Long): Member?

    suspend fun findByIdFetch(id: Long): MemberWithTeam?

    suspend fun search(searchCondition: SearchCondition): List<MemberWithTeam>

    suspend fun searchCount(searchCondition: SearchCondition): Long
}
