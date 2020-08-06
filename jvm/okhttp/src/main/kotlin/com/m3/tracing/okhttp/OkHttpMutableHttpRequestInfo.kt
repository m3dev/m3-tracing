package com.m3.tracing.okhttp

import com.m3.tracing.http.HttpRequestInfo
import com.m3.tracing.http.HttpRequestMetadataKey
import okhttp3.Request

open class OkHttpMutableHttpRequestInfo(
        private var baseReq: Request
) : HttpRequestInfo {
    private var builder: Request.Builder = baseReq.newBuilder()

    override fun tryGetHeader(name: String): String? = baseReq.header(name)
    override fun trySetHeader(name: String, value: String) {
        builder.addHeader(name, value)
    }

    @Suppress("UNCHECKED_CAST", "IMPLICIT_ANY")
    override fun <T> tryGetMetadata(key: HttpRequestMetadataKey<T>): T? = when (key) {
        HttpRequestMetadataKey.Method -> baseReq.method as T?
        HttpRequestMetadataKey.Path -> baseReq.url.encodedPath as T?
        HttpRequestMetadataKey.Host -> baseReq.url.host as T?
        HttpRequestMetadataKey.Url -> baseReq.url.toString() as T?

        else -> null
    }

    fun build(): Request = builder.build()
}
