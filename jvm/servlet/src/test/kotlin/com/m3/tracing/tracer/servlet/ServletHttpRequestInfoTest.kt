package com.m3.tracing.tracer.servlet

import com.google.common.truth.Truth
import com.m3.tracing.http.HttpRequestMetadataKey
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import javax.servlet.http.HttpServletRequest

@ExtendWith(MockitoExtension::class)
class ServletHttpRequestInfoTest {
    val keys = listOf(
            HttpRequestMetadataKey.ContentLength,
            HttpRequestMetadataKey.Host,
            HttpRequestMetadataKey.Method,
            HttpRequestMetadataKey.Path,
            HttpRequestMetadataKey.RemoteAddr,
            HttpRequestMetadataKey.Url
    )

    @Mock lateinit var req: HttpServletRequest
    val info by lazy { ServletHttpRequestInfo(req) }

    @BeforeEach
    fun setupMock() {
        Mockito.`when`(req.method).thenReturn("GET")
        Mockito.`when`(req.serverName).thenReturn("localhost")
        Mockito.`when`(req.contentLength).thenReturn(1024)
        Mockito.`when`(req.requestURI).thenReturn("/test")
        Mockito.`when`(req.remoteAddr).thenReturn("127.0.0.1")
        Mockito.`when`(req.requestURL).thenReturn(StringBuffer("http://localhost/test"))
    }

    @Test
    fun testMetadataValueTypes() {
        keys.forEach { key ->
            val value = info.tryGetMetadata(key)
            Truth.assertThat(value).isInstanceOf(key.type)
        }
    }

}