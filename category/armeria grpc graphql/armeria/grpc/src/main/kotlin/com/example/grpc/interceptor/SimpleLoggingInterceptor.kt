package com.example.grpc.interceptor

import io.grpc.ForwardingServerCall
import io.grpc.ForwardingServerCallListener
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCall.Listener
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import mu.KotlinLogging

class SimpleLoggingInterceptor : ServerInterceptor {

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>,
    ): Listener<ReqT> {
        val serverCall = LoggingServerCall(
            delegate = call,
            startCallMillis = System.currentTimeMillis(),
        )

        return LoggingServerCallListener(next.startCall(serverCall, headers))
    }

    class LoggingServerCall<ReqT, RespT>(
        private val delegate: ServerCall<ReqT, RespT>,
        private val startCallMillis: Long,
    ) : ForwardingServerCall.SimpleForwardingServerCall<ReqT, RespT>(delegate) {

        override fun close(status: Status, trailers: Metadata?) {
            log.info {
                "status:${status.code.name} " +
                    "rpc:${delegate.methodDescriptor.fullMethodName.replace("/", ".")} " +
                    "responseTime:${(System.currentTimeMillis() - startCallMillis)}ms "
            }
            super.close(status, trailers)
        }
    }

    class LoggingServerCallListener<ReqT>(
        delegate: Listener<ReqT>,
    ) : ForwardingServerCallListener.SimpleForwardingServerCallListener<ReqT>(delegate) {

        override fun onMessage(message: ReqT) {
            log.info("Receive Message : ${message.toString().trim()}")
            super.onMessage(message)
        }
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}
