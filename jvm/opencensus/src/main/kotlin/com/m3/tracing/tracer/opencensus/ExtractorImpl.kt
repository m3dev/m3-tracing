package com.m3.tracing.tracer.opencensus

import com.m3.tracing.http.HttpRequestInfo
import com.m3.tracing.http.HttpRequestMetadataKey
import com.m3.tracing.http.HttpResponseInfo
import com.m3.tracing.http.HttpResponseMetadataKey
import io.opencensus.contrib.http.HttpExtractor

internal class ExtractorImpl : HttpExtractor<HttpRequestInfo, HttpResponseInfo>() {
    override fun getHost(request: HttpRequestInfo) = request.tryGetMetadata(HttpRequestMetadataKey.Host)
    override fun getMethod(request: HttpRequestInfo) = request.tryGetMetadata(HttpRequestMetadataKey.Method)

    override fun getPath(request: HttpRequestInfo) = request.tryGetMetadata(HttpRequestMetadataKey.Path)

    override fun getUserAgent(request: HttpRequestInfo) = request.tryGetHeader("User-Agent")

    override fun getStatusCode(response: HttpResponseInfo?) = response?.tryGetMetadata(HttpResponseMetadataKey.StatusCode) ?: 0

    // Intentionally avoided request.getQueryString to preserve character encoding
    // https://stackoverflow.com/a/19409520/914786
    override fun getUrl(request: HttpRequestInfo) = request.url

    override fun getRoute(request: HttpRequestInfo) = ""
}
