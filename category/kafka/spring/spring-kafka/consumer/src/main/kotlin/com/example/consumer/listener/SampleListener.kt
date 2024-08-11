package com.example.consumer.listener

import com.example.consumer.config.ConsumerConfig.Companion.COMMON
import mu.KotlinLogging
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.KafkaListener

@Configuration
class SampleListener {

    private val log = KotlinLogging.logger { }

    @KafkaListener(
        groupId = "backtony-test-single",
        topics = ["backtony-test"],
        containerFactory = COMMON,
    )
    fun sample(article: Article) {
        log.info { article.id }
    }

    @KafkaListener(
        groupId = "backtony-test-batch",
        topics = ["backtony-test"],
        containerFactory = COMMON,
        batch = "true",
    )
    fun sampleBatch(articles: List<Article>) {

        for (article in articles) {
            log.info { "articleId : ${article.id}" }
        }
    }
}
