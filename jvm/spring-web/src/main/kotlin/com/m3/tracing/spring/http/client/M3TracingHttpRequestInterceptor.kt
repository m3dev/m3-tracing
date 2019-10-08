package com.m3.tracing.spring.http.client

import com.m3.tracing.M3Tracer
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse

/**
 * RestTemplate interceptor to trace outgoing HTTP request.
 *
 * If you are not using Spring Boot, need to manually set this interceptor into RestTemplate.
 */
class M3TracingHttpRequestInterceptor(
        private val tracer: M3Tracer
): ClientHttpRequestInterceptor {
    override fun intercept(request: HttpRequest, body: ByteArray, execution: ClientHttpRequestExecution): ClientHttpResponse {
        return tracer.processOutgoingHttpRequest(wrapRequest(request)).use { span ->
            span["client"] = "RestTemplate"
            span["method"] = request.methodValue
            span["uri"] = request.uri.toString()
            execution.execute(request, body).also { response ->
                span["status"] = response.rawStatusCode
            }
        }
    }
    fun wrapRequest(request: HttpRequest) = SpringHttpRequestInfo(request)
}
