package com.m3.tracing.tracer.servlet

import com.m3.tracing.http.HttpRequestInfo
import com.m3.tracing.http.HttpRequestMetadataKey
import jakarta.servlet.http.HttpServletRequest

open class ServletHttpRequestInfo(protected val req: HttpServletRequest): HttpRequestInfo {
    // note: (Java Servlet API problem) Do not call req.getParameter in any case, even after FilterChain.
    // Because servlet might do "forward" request and destination serlvet may call setCharacterEncoding().
    // Thus this filter should not do anything breaks setCharacterEncoding() even after FilterCain.

    override fun tryGetHeader(name: String): String? = req.getHeader(name)
    override fun trySetHeader(name: String, value: String) = Unit // Do nothing

    @Suppress("UNCHECKED_CAST", "IMPLICIT_ANY")
    override fun <T> tryGetMetadata(key: HttpRequestMetadataKey<T>): T? = when(key) {
        HttpRequestMetadataKey.Method -> req.method as T?
        HttpRequestMetadataKey.Host -> req.serverName as T?
        HttpRequestMetadataKey.ContentLength -> req.contentLength.let { if (it <= 0) null else it.toLong() } as T?
        HttpRequestMetadataKey.Path -> req.requestURI as T?
        HttpRequestMetadataKey.RemoteAddr -> req.remoteAddr as T?

        // Intentionally avoided request.getQueryString to preserve character encoding
        // https://stackoverflow.com/a/19409520/914786
        HttpRequestMetadataKey.Url -> req.requestURL.toString() as T?

        else -> null
    }
}
