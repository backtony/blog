package com.example.restclient.factory

import com.example.restclient.config.GrpcProperties
import com.example.restclient.interceptor.LoggingInterceptor
import com.linecorp.armeria.client.ClientFactory
import com.linecorp.armeria.client.grpc.GrpcClients
import org.springframework.stereotype.Component
import java.time.Duration

/**
 * https://armeria.dev/docs/client-grpc/
 */
@Component
class StubFactory(
    private val grpcProperties: GrpcProperties,
) {

    fun <T> createStub(clientType: Class<T & Any>, responseTimeoutMillis: Long = 3000, maxResponseLength: Long = 10485760): T {

        return GrpcClients.builder(grpcProperties.endpoint)
            .responseTimeoutMillis(responseTimeoutMillis)
            .maxResponseLength(maxResponseLength)
            .factory(
                ClientFactory.builder()
                    .apply {
                        // Increase the connect timeout from 3.2s to 5s.
                        connectTimeout(Duration.ofSeconds(5))
                        // Shorten the idle connection timeout from 10s to 5s.
                        idleTimeout(Duration.ofSeconds(5))
                    }
                    .build(),
            )
            .intercept(LoggingInterceptor())
            .build(clientType)
    }
}
