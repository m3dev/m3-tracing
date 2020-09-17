package com.m3.tracing.tracer.servlet

import com.m3.tracing.http.HttpResponseInfo
import com.m3.tracing.http.HttpResponseMetadataKey
import javax.servlet.http.HttpServletResponse

open class ServletHttpResponseInfo(protected val res: HttpServletResponse, private val isOldServletVersion: Boolean = false): HttpResponseInfo {

    @Suppress("UNCHECKED_CAST", "IMPLICIT_ANY")
    override fun <T> tryGetMetadata(key: HttpResponseMetadataKey<T>): T? = when {
        isOldServletVersion -> null
        key == HttpResponseMetadataKey.StatusCode -> res.status as T?
        key == HttpResponseMetadataKey.ContentLength -> res.getHeader("Content-Length")?.toLongOrNull(10) as T?
        else -> null
    }
}
