package com.m3.tracing.okhttp

import com.google.common.truth.Truth
import com.m3.tracing.http.HttpRequestMetadataKey
import okhttp3.Request
import org.junit.jupiter.api.Test

class MutableHttpRequestInfoTest {
    @Test
    fun `each attribute is set properly for Request`() {
        val request = Request.Builder()
                .get()
                .url("http://example.com/foo/bar.html")
                .build()

        val req = MutableHttpRequestInfo(request)

        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Host)).isEqualTo("example.com")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Method)).isEqualTo("GET")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Path)).isEqualTo("/foo/bar.html")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Url)).isEqualTo("http://example.com/foo/bar.html")
    }

    @Test
    fun `header is set properly to Request`() {
        val request = Request.Builder()
                .get()
                .url("http://example.com/foo/bar.html")
                .build()

        val req = MutableHttpRequestInfo(request)

        req.trySetHeader("hoge", "fuga")

        Truth.assertThat(req.build().header("hoge")).isEqualTo("fuga")
    }
}
