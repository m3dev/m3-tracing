package com.m3.tracing.apache.httpclient

import com.google.common.truth.Truth
import com.m3.tracing.http.HttpRequestMetadataKey
import org.apache.http.HttpHost
import org.apache.http.HttpRequest
import org.apache.http.RequestLine
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.client.protocol.HttpClientContext
import org.apache.http.protocol.HttpContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.jupiter.MockitoExtension
import java.net.URI

@ExtendWith(MockitoExtension::class)
class ApacheHttpRequestInfoTest {

    @Mock
    lateinit var uriRequest: HttpUriRequest

    @Mock
    lateinit var nonUriRequest: HttpRequest

    @Mock
    lateinit var clientContext: HttpClientContext

    @Mock
    lateinit var nonClientContext: HttpContext

    @Test
    fun `each attribute is set properly for HttpUriRequest`() {

        val requestLine = mock(RequestLine::class.java)
        val uri = mock(URI::class.java)
        val host = mock(HttpHost::class.java)

        Mockito.`when`(uriRequest.requestLine).thenReturn(requestLine)
        Mockito.`when`(uriRequest.uri).thenReturn(uri)
        Mockito.`when`(clientContext.targetHost).thenReturn(host)
        Mockito.`when`(uri.path).thenReturn("/foo/bar.html")
        Mockito.`when`(requestLine.method).thenReturn("GET")
        Mockito.`when`(host.hostName).thenReturn("test.m3.com")
        Mockito.`when`(host.toURI()).thenReturn("http://test.m3.com")

        val req = ApacheHttpRequestInfo(uriRequest, clientContext)

        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Host)).isEqualTo("test.m3.com")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Method)).isEqualTo("GET")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Path)).isEqualTo("/foo/bar.html")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Url)).isEqualTo("http://test.m3.com/foo/bar.html")
    }

    @Test
    fun `each attribute is set properly for Non-HttpUriRequest`() {

        val requestLine = mock(RequestLine::class.java)

        Mockito.`when`(nonUriRequest.requestLine).thenReturn(requestLine)
        Mockito.`when`(requestLine.method).thenReturn("GET")
        Mockito.`when`(requestLine.uri).thenReturn("/foo/bar.html?param=value")

        val req = ApacheHttpRequestInfo(nonUriRequest, nonClientContext)

        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Host)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Method)).isEqualTo("GET")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Path)).isEqualTo("/foo/bar.html")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Url)).isNull()
    }
}