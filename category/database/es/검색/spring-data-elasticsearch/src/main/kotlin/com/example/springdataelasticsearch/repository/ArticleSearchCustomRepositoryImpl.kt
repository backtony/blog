package com.example.springdataelasticsearch.repository

import co.elastic.clients.elasticsearch._types.query_dsl.QueryBuilders
import co.elastic.clients.json.JsonData
import com.example.springdataelasticsearch.controller.dto.ArticleSearchRequest
import com.example.springdataelasticsearch.controller.dto.AuthorSearchRequest
import com.example.springdataelasticsearch.domain.Article
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder
import org.springframework.data.elasticsearch.core.ElasticsearchOperations
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.elasticsearch.core.routing.RoutingResolver
import org.springframework.stereotype.Repository

/**
 * es는 여러가지 클라이언트를 제공하고 es 8.0이후 버전부터는 java client(elasticSearchClient) 사용을 권장.
 * Spring Data Elasticsearch는 IndexOperations, DocumentOperation, SearchOperations, ElasticSearchOperations(DocumentOperation + SearchOperations) 을 제공하는데
 * 이것은 elasticSearchClient를 사용을 쉽게 하기 위해 추상화된 것이라고 보면 된다.
 */
@Repository
class ArticleSearchCustomRepositoryImpl(
    private val operation: ElasticsearchOperations
) : ArticleSearchCustomRepository {

    // https://docs.spring.io/spring-data/elasticsearch/reference/elasticsearch/routing.html
    override fun findByIdWithRouting(id: String): Article? {
        return operation
            .withRouting(RoutingResolver.just(id))
            .get(id, Article::class.java)
    }

    // https://docs.spring.io/spring-data/elasticsearch/reference/elasticsearch/misc.html#elasticsearch.misc.point-in-time
    override fun search(request: ArticleSearchRequest): List<SearchHit<Article>> {

//        val pit = operation.openPointInTime(IndexCoordinates.of(Article.DOCUMENT), tenSeconds)

        val query = NativeQueryBuilder()
            .withSort(Sort.by(Article::registeredDate.name).descending())
//            .withPointInTime(Query.PointInTime(pit, tenSeconds))
            .withQuery(
                QueryBuilders.bool()
                    .should(QueryBuilders.match().field(Article.TITLE).query(request.keyword).build()._toQuery())
                    .should(QueryBuilders.match().field(Article.BODY).query(request.keyword).build()._toQuery())
                    .build()._toQuery()
            )

        if (request.searchAfter.isEmpty()) {
            query.withPageable(PageRequest.of(request.page, request.size))
        } else {
            query.withSearchAfter(request.searchAfter)
        }

        return operation.search(query.build(), Article::class.java).searchHits
    }

    override fun searchAuthor(request: AuthorSearchRequest): List<SearchHit<Article>> {

        val query = NativeQueryBuilder()
            .withSort(Sort.by(Article::registeredDate.name).descending())
            .withQuery(
                QueryBuilders.nested()
                    .path(Article.Author.DOCUMENT)
                    .query(
                        QueryBuilders.bool()
                            .must(
                                QueryBuilders.range()
                                    .field("${Article.Author.DOCUMENT}.${Article.Author.AGE}")
                                    .gte(JsonData.of(request.startAge))
                                    .lte(JsonData.of(request.endAge))
                                    .build()._toQuery()
                            )
                            .must(
                                QueryBuilders.term()
                                    .field("${Article.Author.DOCUMENT}.${Article.Author.NAME}")
                                    .value(request.keyword)
                                    .build()._toQuery()
                            )
                            .build()._toQuery()
                    ).build()._toQuery()
            )

        if (request.searchAfter.isEmpty()) {
            query.withPageable(PageRequest.of(request.page, request.size))
        } else {
            query.withSearchAfter(request.searchAfter)
        }

        return operation.search(query.build(), Article::class.java).searchHits
    }
}
