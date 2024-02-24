package com.example.springdataelasticsearch.repository

import com.example.springdataelasticsearch.domain.Article
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository

/**
 * 기본적으로 네이밍으로 자동 생성되는 ESRepo는 라우팅이 자동으로 적용되지 않는다.
 */
interface ArticleSearchRepository: ElasticsearchRepository<Article, String>, ArticleSearchCustomRepository {

    fun findByTitle(title: String): List<Article>
    fun findByTitleOrBody(title: String, body: String): List<Article>
}
