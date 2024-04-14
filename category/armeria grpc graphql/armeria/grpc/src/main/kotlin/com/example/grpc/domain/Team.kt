package com.example.grpc.domain

import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.Id
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.relational.core.mapping.Table
import java.time.LocalDateTime

@Table("team")
class Team(
    @Id
    val id: Long? = null,
    val name: String,
    val registeredBy: String,
    @CreatedDate
    val registeredDate: LocalDateTime = LocalDateTime.now(),
    modifiedBy: String,
    @LastModifiedDate
    val modifiedDate: LocalDateTime = LocalDateTime.now(),
) {

    var modifiedBy: String = modifiedBy
        private set
}
