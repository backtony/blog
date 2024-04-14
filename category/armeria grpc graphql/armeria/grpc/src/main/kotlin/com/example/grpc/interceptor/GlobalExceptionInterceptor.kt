package com.example.grpc.interceptor

import io.grpc.ForwardingServerCall.SimpleForwardingServerCall
import io.grpc.Metadata
import io.grpc.ServerCall
import io.grpc.ServerCallHandler
import io.grpc.ServerInterceptor
import io.grpc.Status
import mu.KotlinLogging

class GlobalExceptionInterceptor : ServerInterceptor {

    override fun <ReqT : Any?, RespT : Any?> interceptCall(
        call: ServerCall<ReqT, RespT>,
        headers: Metadata,
        next: ServerCallHandler<ReqT, RespT>,
    ): ServerCall.Listener<ReqT> {

        return next.startCall(ExceptionServerCall(call), headers)
    }

    class ExceptionServerCall<ReqT, RespT>(
        delegate: ServerCall<ReqT, RespT>,
    ) : SimpleForwardingServerCall<ReqT, RespT>(delegate) {

        override fun close(status: Status, trailers: Metadata?) {
            if (status.isOk) {
                super.close(status, trailers)
            } else {
                val exceptionStatus: Status = handleException(status.cause)
                log.error("gRPC exception : \n$exceptionStatus", status.cause)
                super.close(exceptionStatus, trailers)
            }
        }

        /**
         * Exception을 grpc error Code로 변경
         */
        private fun handleException(e: Throwable?): Status {
            when (e) {
                is IllegalArgumentException -> return Status.INVALID_ARGUMENT.withDescription(e.message)
            }

            return Status.INTERNAL.withDescription(e?.message)
        }
    }

    companion object {
        private val log = KotlinLogging.logger { }
    }
}
