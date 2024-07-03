package com.sample.hexagonal.sample.server.batch.interceptor

import com.sample.hexagonal.sample.server.batch.exception.InvalidRefererException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor
import java.net.URI

class RefererInspectionInterceptor(
    private val allowedRefererHost: String,
) : HandlerInterceptor {

    override fun preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Any): Boolean {
        return if (handler is HandlerMethod) {
            if (handler.getMethodAnnotation(RefererCheck::class.java) != null) {
                val refererHost = runCatching {
                    URI(request.getHeader(HttpHeaders.REFERER)).host
                }.getOrElse {
                    throw InvalidRefererException()
                }

                if (!refererHost.contains(allowedRefererHost)) {
                    throw InvalidRefererException()
                }
            }
            true
        } else {
            false
        }
    }
}
