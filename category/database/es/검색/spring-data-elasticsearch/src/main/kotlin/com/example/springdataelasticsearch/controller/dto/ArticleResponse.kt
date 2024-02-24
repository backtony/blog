package com.example.springdataelasticsearch.controller.dto

import java.time.LocalDateTime

data class ArticleResponse(
    val id: Long,
    var title: String,
    var body: String,
    val authors: List<Author>,
    val attachment: Attachment,
    val registeredDate: LocalDateTime,
    val lastModifiedDate: LocalDateTime,
) {

    data class Author(
        val name: String,
        val age: Int
    )

    data class Attachment(
        val name: String,
        val path: String
    )
}

