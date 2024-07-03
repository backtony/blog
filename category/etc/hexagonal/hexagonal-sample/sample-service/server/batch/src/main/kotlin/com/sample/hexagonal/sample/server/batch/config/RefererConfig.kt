package com.sample.hexagonal.sample.server.batch.config

import com.sample.hexagonal.sample.server.batch.interceptor.RefererInspectionInterceptor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.web.servlet.config.annotation.InterceptorRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

@Profile("!local")
@Configuration
class RefererConfig(
    @Value("\${referer.ctmagent}") private val referer: String,
) : WebMvcConfigurer {

    override fun addInterceptors(registry: InterceptorRegistry) {
        registry.addInterceptor(RefererInspectionInterceptor(referer))
    }
}
