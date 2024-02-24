package com.example.springdataelasticsearch.repository

import com.example.springdataelasticsearch.controller.dto.ArticleSearchRequest
import com.example.springdataelasticsearch.controller.dto.AuthorSearchRequest
import com.example.springdataelasticsearch.domain.Article
import org.springframework.data.elasticsearch.core.SearchHit

interface ArticleSearchCustomRepository {

    fun findByIdWithRouting(id: String) : Article?

    fun search(articleSearchRequest: ArticleSearchRequest): List<SearchHit<Article>>
    fun searchAuthor(authorSearchRequest: AuthorSearchRequest): List<SearchHit<Article>>
}
