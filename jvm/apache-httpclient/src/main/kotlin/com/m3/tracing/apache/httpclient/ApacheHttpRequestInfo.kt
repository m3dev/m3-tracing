package com.m3.tracing.apache.httpclient

import com.m3.tracing.http.HttpRequestInfo
import com.m3.tracing.http.HttpRequestMetadataKey
import org.apache.http.HttpRequest
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.protocol.HttpContext

open class ApacheHttpRequestInfo(protected val req: HttpRequest, protected val context: HttpContext) : HttpRequestInfo {
    override fun tryGetHeader(name: String): String? = req.getFirstHeader(name)?.value
    override fun trySetHeader(name: String, value: String) = req.setHeader(name, value)

    @Suppress("UNCHECKED_CAST", "IMPLICIT_ANY")
    override fun <T> tryGetMetadata(key: HttpRequestMetadataKey<T>): T? = when (key) {
        HttpRequestMetadataKey.Method -> req.requestLine?.method as T?
        HttpRequestMetadataKey.Path -> getPath() as T?
        HttpRequestMetadataKey.Host -> if (context is HttpClientContext) context.targetHost?.hostName as T? else null
        HttpRequestMetadataKey.Url -> if (context is HttpClientContext) "${context.targetHost}${getPath() ?: ""}" as T? else null

        else -> null
    }

    // HttpRequest.requestLine.url contains querystring, so need to be removed
    private fun getPath(): String? = if (req is HttpUriRequest) req.uri?.path else req.requestLine?.uri?.split("?")?.elementAt(0)
}
