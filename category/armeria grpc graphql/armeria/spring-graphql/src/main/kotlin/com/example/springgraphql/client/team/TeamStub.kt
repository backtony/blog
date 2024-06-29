package com.example.springgraphql.client.team

import com.example.proto.team.TeamHandlerGrpcKt
import com.example.springgraphql.client.StubFactory
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component

@Component
class TeamStub(
    private val stubFactory: StubFactory,
) {

    @Bean
    fun teamServiceStub(): TeamHandlerGrpcKt.TeamHandlerCoroutineStub {
        return stubFactory.createStub(TeamHandlerGrpcKt.TeamHandlerCoroutineStub::class)
    }
}
