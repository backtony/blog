package com.example.grpc.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("member")
class Member(
    @Id
    val id: Long? = null,
    val name: String,
    introduction: String? = null,
    val type: Type,
    val country: Country,
    teamId: Long? = null,
    val registeredBy: String,
    @CreatedDate
    val registeredDate: LocalDateTime = LocalDateTime.now(),
    modifiedBy: String,
    @LastModifiedDate
    val modifiedDate: LocalDateTime = LocalDateTime.now(),
) {

    var teamId: Long? = teamId
        private set

    var introduction: String? = introduction
        private set

    var modifiedBy: String = modifiedBy
        private set

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
