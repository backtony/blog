package com.example.grpc.config

import com.example.grpc.interceptor.GlobalExceptionInterceptor
import com.example.grpc.interceptor.SimpleLoggingInterceptor
import com.linecorp.armeria.server.docs.DocService
import com.linecorp.armeria.server.grpc.GrpcService
import com.linecorp.armeria.server.logging.LoggingService
import com.linecorp.armeria.spring.ArmeriaServerConfigurator
import io.asyncer.r2dbc.mysql.client.Client.logger
import io.grpc.kotlin.AbstractCoroutineServerImpl
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ArmeriaConfig {

    @Bean
    fun grpcService(
        allServiceBean: List<AbstractCoroutineServerImpl>,
    ): GrpcService {
        val grpcServiceBuilder = GrpcService.builder()
            .enableUnframedRequests(true)
            .intercept(SimpleLoggingInterceptor(), GlobalExceptionInterceptor())

        allServiceBean.forEach {
            logger.info("Register Grpc Bean : {}", it.javaClass.name)
            grpcServiceBuilder.addService(it)
        }

        return grpcServiceBuilder.build()
    }

    @Bean
    fun armeriaServerConfigurator(
        grpcService: GrpcService,
    ): ArmeriaServerConfigurator {
        // TODO serverBuilder에 들어가보면 server 관련 옵션들을 추가할 수 있음.
        return ArmeriaServerConfigurator {

            /**
             * Max Request Length 증설
             */
            it.maxRequestLength(32 * 1024 * 1024)

            /**
             * Grpc 사용을 위한 서비스 등록
             */
            it.service(grpcService)

            /**
             * Docs 생성을 위한 서비스 등록
             */
            it.serviceUnder("/docs", DocService())

            /**
             * Logging을 위한 Decorator 등록
             */
            // com.linecorp.armeria.server.logging.LoggingService 로그레벨 별도 지정 or 아래와 같이 사용가능
            it.decorator(LoggingService.newDecorator())
//            it.decorator(LoggingService.builder()
//                .requestLogLevel(LogLevel.INFO)  // 요청 로그 레벨 설정
//                .successfulResponseLogLevel(LogLevel.INFO)  // 성공 응답 로그 레벨 설정
//                .failureResponseLogLevel(LogLevel.ERROR)  // 실패 응답 로그 레벨 설정
//                .newDecorator()
//            )
        }
    }
}
