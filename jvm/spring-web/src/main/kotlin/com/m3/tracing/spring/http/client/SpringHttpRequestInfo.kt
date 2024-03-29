package com.m3.tracing.spring.http.client

import com.m3.tracing.http.HttpRequestInfo
import com.m3.tracing.http.HttpRequestMetadataKey
import org.springframework.http.HttpRequest

open class SpringHttpRequestInfo(protected val req: HttpRequest): HttpRequestInfo {
    override fun tryGetHeader(name: String): String? = req.headers.getFirst(name)
    override fun trySetHeader(name: String, value: String) = req.headers.set(name, value)

    @Suppress("UNCHECKED_CAST", "IMPLICIT_ANY")
    override fun <T> tryGetMetadata(key: HttpRequestMetadataKey<T>): T? = when(key) {
        HttpRequestMetadataKey.Method -> req.method.toString() as T?
        HttpRequestMetadataKey.Host -> req.headers.host?.hostName as T?
        HttpRequestMetadataKey.ContentLength -> req.headers.contentLength.let { if (it <= 0) null else it } as T?
        HttpRequestMetadataKey.Path -> req.uri.path as T?
        HttpRequestMetadataKey.Url -> req.uri.toString() as T?

        else -> null
    }
}
