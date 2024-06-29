package com.example.springgraphql.client.member

import com.example.proto.member.MemberHandlerGrpcKt
import com.example.springgraphql.client.StubFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class MemberStub(
    private val stubFactory: StubFactory,
) {

    @Bean
    fun memberServiceStub(): MemberHandlerGrpcKt.MemberHandlerCoroutineStub {
        return stubFactory.createStub(MemberHandlerGrpcKt.MemberHandlerCoroutineStub::class)
    }
}
