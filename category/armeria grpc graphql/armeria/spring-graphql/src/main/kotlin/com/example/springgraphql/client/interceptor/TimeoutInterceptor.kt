package com.example.restclient.interceptor

import io.grpc.CallOptions
import io.grpc.Channel
import io.grpc.ClientCall
import io.grpc.ClientInterceptor
import io.grpc.MethodDescriptor
import java.util.concurrent.TimeUnit

class TimeoutInterceptor(
    private val timeout: Long,
) : ClientInterceptor {

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        method: MethodDescriptor<ReqT, RespT>,
        callOptions: CallOptions,
        next: Channel,
    ): ClientCall<ReqT, RespT> {

        return next.newCall(method, callOptions.withDeadlineAfter(timeout, TimeUnit.MILLISECONDS))
    }
}
