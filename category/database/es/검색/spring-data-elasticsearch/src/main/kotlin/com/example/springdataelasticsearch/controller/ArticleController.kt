package com.example.springdataelasticsearch.controller

import com.example.springdataelasticsearch.controller.dto.ArticleResponse
import com.example.springdataelasticsearch.controller.dto.ArticleSaveRequest
import com.example.springdataelasticsearch.controller.dto.ArticleSearchRequest
import com.example.springdataelasticsearch.controller.dto.ArticleUpdateRequest
import com.example.springdataelasticsearch.controller.dto.AuthorSearchRequest
import com.example.springdataelasticsearch.domain.Article
import com.example.springdataelasticsearch.service.ArticleService
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class ArticleController(
    private val articleService: ArticleService,
) {

    // CREATE, UPDATE, DELETE SAMPLE

    @PostMapping("/articles/dummy")
    fun saveDummy(): List<ArticleResponse> {
        return articleService.saveDummy()
            .map { ArticleMapper.mapToArticleResponse(it) }
    }

    @PostMapping("/articles")
    fun save(@RequestBody request: ArticleSaveRequest): ArticleResponse {
        return articleService.save(request)
            .let { ArticleMapper.mapToArticleResponse(it) }
    }

    @PatchMapping("/articles/{id}")
    fun update(@PathVariable id: String, @RequestBody request: ArticleUpdateRequest): ArticleResponse? {
        return articleService.update(id, request)
            .let { ArticleMapper.mapToArticleResponse(it) }
    }

    @DeleteMapping("/articles/{id}")
    fun delete(@PathVariable id: String) {
        return articleService.delete(id)
    }

    /**
     * READ SAMPLE
     */

    @GetMapping("/articles/{id}")
    fun get(@PathVariable id: String): ArticleResponse {
        return articleService.get(id)
            .let { ArticleMapper.mapToArticleResponse(it) }
    }

    @GetMapping("/articles/{id}/routing")
    fun getWithRouting(@PathVariable id: String): ArticleResponse {
        return articleService.getByIdWithRouting(id)
            .let { ArticleMapper.mapToArticleResponse(it) }
    }

    @GetMapping("/articles/search")
    fun searchArticles(
        articleSearchRequest: ArticleSearchRequest,
    ): List<SearchHit<Article>> {
        return articleService.search(articleSearchRequest)
    }

    @GetMapping("/articles/author")
    fun searchAuthor(
        authorSearchRequest: AuthorSearchRequest
    ): List<SearchHit<Article>> {
        return articleService.searchAuthor(authorSearchRequest)
    }
}
