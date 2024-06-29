package com.example.springgraphql.controller

import com.example.springgraphql.api.HelloMutationResolver
import com.example.springgraphql.api.HelloQueryResolver
import graphql.schema.DataFetchingEnvironment
import org.springframework.stereotype.Controller

@Controller
class HelloController : HelloQueryResolver, HelloMutationResolver {

    override suspend fun createHello(env: DataFetchingEnvironment): String {
        return "hello"
    }

    override suspend fun hello(env: DataFetchingEnvironment): String {
        return "hello"
    }
}
