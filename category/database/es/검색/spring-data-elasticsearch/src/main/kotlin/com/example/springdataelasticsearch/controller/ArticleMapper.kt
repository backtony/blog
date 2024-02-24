package com.example.springdataelasticsearch.controller

import com.example.springdataelasticsearch.controller.dto.ArticleResponse
import com.example.springdataelasticsearch.domain.Article

object ArticleMapper {

    fun mapToArticleResponse(article: Article): ArticleResponse {
        return with(article) {
            ArticleResponse(
                id = id,
                title = title,
                body = body,
                authors = authors.map{ ArticleResponse.Author(
                    it.name,
                    it.age
                )},
                attachment = ArticleResponse.Attachment(
                    path = attachment.path,
                    name = attachment.name
                ),
                registeredDate = registeredDate.toLocalDateTime(),
                lastModifiedDate = lastModifiedDate.toLocalDateTime(),
            )
        }
    }
}
