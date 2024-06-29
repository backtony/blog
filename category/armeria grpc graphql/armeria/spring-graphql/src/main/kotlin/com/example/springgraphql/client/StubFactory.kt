package com.example.springgraphql.client

import com.example.restclient.interceptor.TimeoutInterceptor
import com.example.springgraphql.config.grpc.GrpcProperties
import io.grpc.CallOptions
import io.grpc.ManagedChannel
import io.grpc.kotlin.AbstractCoroutineStub
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import kotlin.reflect.KClass
import kotlin.reflect.full.primaryConstructor


@Component
class StubFactory(
    private val grpcProperties: GrpcProperties,
    private val grpcChannel: ManagedChannel,
) {

    /**
     * withDeadlineAfter를 적용하거나 callOption으로 deadline을 적용하고 빈으로 등록하는 경우 빈 등록 시점부터 카운트가 들어가므로
     * timeout옵션은 interceptor로 부여하거나 사용처마다 withDeadlineAfter를 별도로 정의하고 사용해야 한다.
     */
    fun <T> createStub(
        stubClass: KClass<T>,
        timeout: Long = grpcProperties.timeout,
    ): T where T : AbstractCoroutineStub<T> {
        val constructor = stubClass.primaryConstructor!!
        return constructor.call(grpcChannel, CallOptions.DEFAULT)
            .withInterceptors(TimeoutInterceptor(timeout))
    }
}
