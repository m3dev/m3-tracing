package com.m3.tracing.tracer.opencensus

import com.google.common.truth.Truth
import com.m3.tracing.http.HttpRequestInfo
import com.m3.tracing.http.HttpRequestMetadataKey
import com.m3.tracing.http.HttpResponseInfo
import com.m3.tracing.http.HttpResponseMetadataKey
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class ExtractorImplTest {

    @Spy // Should be Spy to enable interface default method
    lateinit var requestInfo: HttpRequestInfo
    @Spy
    lateinit var responseInfo: HttpResponseInfo

    private val extractor = ExtractorImpl()

    @Test
    fun testHost() {
        Mockito.`when`(requestInfo.tryGetMetadata(HttpRequestMetadataKey.Host)).thenReturn("example.com")

        Truth.assertThat(extractor.getHost(requestInfo)).isEqualTo("example.com")
    }

    @Test
    fun testMethod() {
        Mockito.`when`(requestInfo.tryGetMetadata(HttpRequestMetadataKey.Method)).thenReturn("PUT")

        Truth.assertThat(extractor.getMethod(requestInfo)).isEqualTo("PUT")
    }

    @Test
    fun testPath() {
        Mockito.`when`(requestInfo.tryGetMetadata(HttpRequestMetadataKey.Path)).thenReturn("/path/to/page")

        Truth.assertThat(extractor.getPath(requestInfo)).isEqualTo("/path/to/page")
    }

    @Test
    fun testUserAgent() {
        Mockito.`when`(requestInfo.tryGetHeader("User-Agent")).thenReturn("test UA/0.0")

        Truth.assertThat(extractor.getUserAgent(requestInfo)).isEqualTo("test UA/0.0")
    }

    @Test
    fun testUserAgentUnavailable() {
        Mockito.`when`(requestInfo.tryGetHeader("User-Agent")).thenReturn(null)

        Truth.assertThat(extractor.getUserAgent(requestInfo)).isEqualTo(null)
    }

    @Test
    fun testStatusCode() {
        Mockito.`when`(responseInfo.tryGetMetadata(HttpResponseMetadataKey.StatusCode)).thenReturn(201)

        Truth.assertThat(extractor.getStatusCode(responseInfo)).isEqualTo(201)
    }

    @Test
    fun testStatusCodeUnavailable() {
        Mockito.`when`(responseInfo.tryGetMetadata(HttpResponseMetadataKey.StatusCode)).thenReturn(null)

        Truth.assertThat(extractor.getStatusCode(responseInfo)).isEqualTo(0)
    }

    @Test
    fun testStatusCodeWithoutResponseInfo() {
        Truth.assertThat(extractor.getStatusCode(null)).isEqualTo(0)
    }

    @Test
    fun testUrl() {
        Mockito.`when`(requestInfo.tryGetMetadata(HttpRequestMetadataKey.Url)).thenReturn("http://example.com/path/to/page")

        Truth.assertThat(extractor.getUrl(requestInfo)).isEqualTo("http://example.com/path/to/page")
    }

    @Test
    fun testRoute() {
        Truth.assertThat(extractor.getRoute(requestInfo)).isEqualTo("")
    }
}