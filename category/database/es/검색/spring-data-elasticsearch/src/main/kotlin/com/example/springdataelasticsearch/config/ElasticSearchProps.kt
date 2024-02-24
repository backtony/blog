package com.example.springdataelasticsearch.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "elasticsearch")
data class ElasticSearchProps(
    val uris: String,
    val username: String,
    val password: String,
    val connectTimeout: Int,
    val connectionRequestTimeout: Int,
    val socketTimeout: Int,
)
