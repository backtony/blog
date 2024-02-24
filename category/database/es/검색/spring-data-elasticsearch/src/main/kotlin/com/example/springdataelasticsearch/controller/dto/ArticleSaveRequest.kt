package com.example.springdataelasticsearch.controller.dto

data class ArticleSaveRequest(
    val title: String,
    val body: String,
    val authors: List<Author>,
    val attachment: Attachment
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
