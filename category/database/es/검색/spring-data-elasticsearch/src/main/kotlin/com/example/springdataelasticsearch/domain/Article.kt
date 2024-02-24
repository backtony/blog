package com.example.springdataelasticsearch.domain

import com.example.springdataelasticsearch.domain.Article.Companion.DOCUMENT
import org.springframework.data.annotation.Id
import org.springframework.data.elasticsearch.annotations.Document
import org.springframework.data.elasticsearch.annotations.Field
import org.springframework.data.elasticsearch.annotations.FieldType
import org.springframework.data.elasticsearch.annotations.Routing
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime

@Document(indexName = DOCUMENT)
// https://docs.spring.io/spring-data/elasticsearch/reference/elasticsearch/routing.html#elasticsearch.routing.custom
@Routing("id") // save 시에 자동으로 id값을 routing으로 넣어주는 역할 -> elasticsearchRepository의 findXX 는 routing을 지원하지 않음.
data class Article(
    @Id
    val id: Long,
    var title: String,
    var body: String,
    val authors: List<Author>, // nested type
    val attachment: Attachment, // object type
    // https://docs.spring.io/spring-data/elasticsearch/docs/4.4.x-SNAPSHOT/api/index.html?org/springframework/data/elasticsearch/annotations/DateFormat.html
    // format custom이 deprecated되어서 custom을 사용하려면 [] 빈 리스트 사용
    @Field(type = FieldType.Date, format = [], pattern = ["yyyy-MM-dd HH:mm:ss.SSSZ"])
    val registeredDate: ZonedDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()),
    @Field(type = FieldType.Date, format = [], pattern = ["yyyy-MM-dd HH:mm:ss.SSSZ"])
    var lastModifiedDate: ZonedDateTime = LocalDateTime.now().atZone(ZoneId.systemDefault()),
    val tieBreaker: Long,
) {

    fun update(title: String, body: String): Article {
        this.title = title
        this.body = body
        this.lastModifiedDate = LocalDateTime.now().atZone(ZoneId.systemDefault())
        return this
    }

    data class Attachment(
        val name: String,
        val path: String
    )

    data class Author(
        val name: String,
        val age: Int
    ) {
        companion object {
            const val DOCUMENT = "authors"
            const val AGE = "age"
            const val NAME = "name"
        }
    }


    companion object {
        const val DOCUMENT = "article"
        const val TITLE = "title"
        const val BODY = "body"
    }
}


