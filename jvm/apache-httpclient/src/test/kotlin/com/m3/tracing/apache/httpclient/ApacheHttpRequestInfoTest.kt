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
        Mockito.`when`(host.hostName).thenReturn("example.com")
        Mockito.`when`(host.toString()).thenReturn("http://example.com")

        val req = ApacheHttpRequestInfo(uriRequest, clientContext)

        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Host)).isEqualTo("example.com")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Method)).isEqualTo("GET")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Path)).isEqualTo("/foo/bar.html")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Url)).isEqualTo("http://example.com/foo/bar.html")
    }

    @Test
    fun `each attribute is set properly for Non-HttpUriRequest`() {

        val requestLine = mock(RequestLine::class.java)

        Mockito.`when`(nonUriRequest.requestLine).thenReturn(requestLine)
        Mockito.`when`(requestLine.method).thenReturn("GET")
        Mockito.`when`(requestLine.uri).thenReturn("/foo/bar.html?param=value")

        var req = ApacheHttpRequestInfo(nonUriRequest, nonClientContext)

        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Host)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Method)).isEqualTo("GET")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Path)).isEqualTo("/foo/bar.html")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Url)).isNull()

        Mockito.`when`(requestLine.uri).thenReturn("/")

        req = ApacheHttpRequestInfo(nonUriRequest, nonClientContext)

        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Host)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Method)).isEqualTo("GET")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Path)).isEqualTo("/")
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Url)).isNull()
    }

    @Test
    fun `no exception when attributes in HttpUriRequest are null`() {

        val requestLine = mock(RequestLine::class.java)
        val uri = mock(URI::class.java)
        val host = mock(HttpHost::class.java)

        Mockito.`when`(uriRequest.requestLine).thenReturn(requestLine)
        Mockito.`when`(uriRequest.uri).thenReturn(uri)
        Mockito.`when`(clientContext.targetHost).thenReturn(host)
        Mockito.`when`(uri.path).thenReturn(null)
        Mockito.`when`(requestLine.method).thenReturn(null)
        Mockito.`when`(host.hostName).thenReturn(null)
        Mockito.`when`(host.toString()).thenReturn(null)

        var req = ApacheHttpRequestInfo(uriRequest, clientContext)

        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Host)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Method)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Path)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Url)).isEqualTo("null")

        Mockito.`when`(uriRequest.requestLine).thenReturn(null)
        Mockito.`when`(uriRequest.uri).thenReturn(null)
        Mockito.`when`(clientContext.targetHost).thenReturn(null)

        req = ApacheHttpRequestInfo(uriRequest, clientContext)

        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Host)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Method)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Path)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Url)).isEqualTo("null")
    }

    @Test
    fun `no exception when attributes in non-HttpUriRequest are null`() {

        val requestLine = mock(RequestLine::class.java)

        Mockito.`when`(nonUriRequest.requestLine).thenReturn(requestLine)
        Mockito.`when`(requestLine.method).thenReturn(null)
        Mockito.`when`(requestLine.uri).thenReturn(null)

        val req = ApacheHttpRequestInfo(nonUriRequest, nonClientContext)

        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Host)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Method)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Path)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Url)).isNull()

        Mockito.`when`(nonUriRequest.requestLine).thenReturn(null)

        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Host)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Method)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Path)).isNull()
        Truth.assertThat(req.tryGetMetadata(HttpRequestMetadataKey.Url)).isNull()
    }
}
