package com.example.springdataelasticsearch.controller.dto

data class ArticleSearchRequest(
    val page: Int,
    val size: Int,
    val keyword: String,
    val searchAfter: List<Any> = emptyList(),
)
