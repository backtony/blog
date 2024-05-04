package com.example.restclient.client.member

import com.example.proto.member.MemberHandlerGrpcKt
import com.example.restclient.factory.StubFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class MemberStub(
    private val stubFactory: StubFactory
) {

    @Bean
    fun memberServiceStub(): MemberHandlerGrpcKt.MemberHandlerCoroutineStub {
        return stubFactory.createStub(MemberHandlerGrpcKt.MemberHandlerCoroutineStub::class)
    }
}
