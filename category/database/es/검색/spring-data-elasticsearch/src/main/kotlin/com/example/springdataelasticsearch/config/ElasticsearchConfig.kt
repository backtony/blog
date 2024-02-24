package com.example.springdataelasticsearch.config

import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.config.RequestConfig
import org.apache.http.impl.client.BasicCredentialsProvider
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy
import org.apache.http.impl.nio.reactor.IOReactorConfig
import org.springframework.context.annotation.Configuration
import org.springframework.data.elasticsearch.client.ClientConfiguration
import org.springframework.data.elasticsearch.client.elc.ElasticsearchClients
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration


@Configuration
class ElasticsearchConfig(
    private val elasticSearchProps: ElasticSearchProps
) : ElasticsearchConfiguration() {

    // https://docs.spring.io/spring-data/elasticsearch/reference/elasticsearch/clients.html
    override fun clientConfiguration(): ClientConfiguration {

        val requestConfig = RequestConfig.custom()
            .setConnectTimeout(elasticSearchProps.connectTimeout)
            .setConnectionRequestTimeout(elasticSearchProps.connectionRequestTimeout)
            .setSocketTimeout(elasticSearchProps.socketTimeout)
            .build()

        return ClientConfiguration.builder()
            .connectedTo(elasticSearchProps.uris)
            .withBasicAuth(elasticSearchProps.username, elasticSearchProps.password)
            .withClientConfigurer(
                ElasticsearchClients.ElasticsearchHttpClientConfigurationCallback.from {
                    it.setDefaultRequestConfig(requestConfig)
                        .setDefaultIOReactorConfig(IOReactorConfig.custom().setSoKeepAlive(true).build())
                        .setKeepAliveStrategy(DefaultConnectionKeepAliveStrategy())
                }
            )
            .build()
    }
}
