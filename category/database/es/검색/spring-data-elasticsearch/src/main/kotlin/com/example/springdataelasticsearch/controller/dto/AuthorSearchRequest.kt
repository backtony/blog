package com.example.springdataelasticsearch.controller.dto

data class AuthorSearchRequest(
    val page: Int,
    val size: Int,
    val keyword: String,
    val startAge: Int,
    val endAge: Int,
    val searchAfter: List<Any> = emptyList(),
)
