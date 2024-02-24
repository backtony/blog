package com.example.springdataelasticsearch.service

import com.example.springdataelasticsearch.controller.dto.ArticleSaveRequest
import com.example.springdataelasticsearch.controller.dto.ArticleSearchRequest
import com.example.springdataelasticsearch.controller.dto.ArticleUpdateRequest
import com.example.springdataelasticsearch.controller.dto.AuthorSearchRequest
import com.example.springdataelasticsearch.domain.Article
import com.example.springdataelasticsearch.repository.ArticleSearchRepository
import org.springframework.data.elasticsearch.core.SearchHit
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.util.*
import java.util.concurrent.atomic.AtomicLong

@Service
class ArticleService(
    private val articleRepository: ArticleSearchRepository
) {

    // 보통 es 색인은 RDBMS에 있는 데이터를 기준으로 색인하므로 이미 id값이 있으므로 여기서는 id값을 예시로 추출
    private var articleSequence: AtomicLong = AtomicLong(1L)

    fun saveDummy(): List<Article> {

        val dummy = mutableListOf<Article>()

        var id = articleSequence.getAndIncrement()
        dummy.add(
            Article(
                id = id,
                title = "나 혼자 천재 DNA",
                body = "내 몸에 스며든 생명세포 로잘린",
                authors = listOf(Article.Author("지운성", 25), Article.Author("임아도", 26)),
                attachment = Article.Attachment(
                    name = "첨부파일.png",
                    path = UUID.randomUUID().toString()
                ),
                tieBreaker = id
            )
        )

        id = articleSequence.getAndIncrement()
        dummy.add(
            Article(
                id = id,
                title = "나 혼자만 레벨업",
                body = "100년 전, 다른 차원과 이쪽 세계를.. DNA",
                authors = listOf(Article.Author("현군",  (20..50).random()), Article.Author("장성락",(20..50).random())),
                attachment = Article.Attachment(
                    name = "첨부파일.png",
                    path = UUID.randomUUID().toString()
                ),
                tieBreaker = id
            )
        )

        id = articleSequence.getAndIncrement()
        dummy.add(
            Article(
                id = id,
                title = "나 혼자 소설 속 망나니",
                body = "매일 같은 야근으로 밤을 새던 도중..",
                authors = listOf(Article.Author("알그", 24), Article.Author("소유현", 27)),
                attachment = Article.Attachment(
                    name = "첨부파일.png",
                    path = UUID.randomUUID().toString()
                ),
                tieBreaker = id
            )
        )

        id = articleSequence.getAndIncrement()
        dummy.add(
            Article(
                id = id,
                title = "나 혼자 만렙 귀환자",
                body = "전 세계 1억 2천만 명으 인구가 사라졌다..",
                authors = listOf(Article.Author("이지득", 24), Article.Author("vertwo", 27)),
                attachment = Article.Attachment(
                    name = "첨부파일.png",
                    path = UUID.randomUUID().toString()
                ),
                tieBreaker = id
            )
        )

        for (i in 1 until 30) {
            id = articleSequence.getAndIncrement()
            dummy.add(
                Article(
                    id = id,
                    title = "나 혼자 만렙 귀환자",
                    body = "전 세계 1억 2천만 명으 인구가 사라졌다..aa",
                    authors = listOf(Article.Author("이지득", 24), Article.Author("vertwo", 27)),
                    attachment = Article.Attachment(
                        name = "첨부파일.png",
                        path = UUID.randomUUID().toString()
                    ),
                    tieBreaker = id
                )
            )
        }

        return articleRepository.saveAll(dummy).toList()
    }

    fun save(request: ArticleSaveRequest): Article {
        val id = articleSequence.getAndIncrement()

        val article = with(request) {
            Article(
                id = id,
                title = title,
                body = body,
                authors = authors.map { Article.Author(it.name, it.age) },
                attachment = Article.Attachment(
                    name = attachment.name,
                    path = attachment.path
                ),
                tieBreaker = id
            )
        }

        return articleRepository.save(article)
    }

    fun update(id: String, request: ArticleUpdateRequest): Article {

        val article = articleRepository.findByIdOrNull(id) ?: throw RuntimeException("$id not found")
        return articleRepository.save(article.update(request.title, request.body))
    }

    fun delete(id: String) {
        articleRepository.deleteById(id)
    }

    fun get(id: String): Article {

        return articleRepository.findByIdOrNull(id) ?: throw RuntimeException("$id not found")
    }

    fun getByIdWithRouting(id:String): Article {
        return articleRepository.findByIdWithRouting(id) ?: throw RuntimeException("not found")
    }

    fun search(articleSearchRequest: ArticleSearchRequest): List<SearchHit<Article>> {
        return articleRepository.search(articleSearchRequest)
    }

    fun searchAuthor(authorSearchRequest: AuthorSearchRequest): List<SearchHit<Article>> {
        return articleRepository.searchAuthor(authorSearchRequest)
    }
}
