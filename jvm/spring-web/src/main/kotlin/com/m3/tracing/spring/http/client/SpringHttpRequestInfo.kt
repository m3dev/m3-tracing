package com.m3.tracing.spring.http.client

import com.m3.tracing.http.HttpRequestInfo
import com.m3.tracing.http.HttpRequestMetadataKey
import com.m3.tracing.http.MutableHttpRequestInfo
import org.slf4j.LoggerFactory
import org.springframework.http.HttpRequest

open class SpringHttpRequestInfo(protected val req: HttpRequest): MutableHttpRequestInfo {
    companion object {
        private val acceptableHeaders = listOf(
                "traceparent", "tracestate",
                "X-B3-TraceId", "X-B3-SpanId", "X-B3-Sampled",
                "X-Cloud-Trace-Context"
        )
        private val logger = LoggerFactory.getLogger(SpringHttpRequestInfo::class.java)
    }

    override fun tryGetHeader(name: String): String? = req.headers.getFirst(name)
    override fun trySetHeader(name: String, value: String) = if (acceptableHeaders.contains(name)) {
        req.headers.set(name, value)
    } else {
        logger.error("Failed to set header name: ${name}, value: ${value}, it's not accepatable")
    }

    @Suppress("UNCHECKED_CAST", "IMPLICIT_ANY")
    override fun <T> tryGetMetadata(key: HttpRequestMetadataKey<T>): T? = when(key) {
        HttpRequestMetadataKey.Method -> req.methodValue as T?
        HttpRequestMetadataKey.Host -> req.headers.host?.hostName as T?
        HttpRequestMetadataKey.ContentLength -> req.headers.contentLength.let { if (it <= 0) null else it } as T?
        HttpRequestMetadataKey.Path -> req.uri.path as T?
        HttpRequestMetadataKey.Url -> req.uri.toString() as T?

        else -> null
    }
}
