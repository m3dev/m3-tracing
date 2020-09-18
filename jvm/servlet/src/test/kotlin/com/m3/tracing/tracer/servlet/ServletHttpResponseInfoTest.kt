package com.m3.tracing.tracer.servlet

import com.google.common.truth.Truth
import com.m3.tracing.http.HttpResponseMetadataKey
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import javax.servlet.http.HttpServletResponse

@ExtendWith(MockitoExtension::class)
class ServletHttpResponseInfoTest {

    @Mock lateinit var res: HttpServletResponse

    @Test
    fun testMetadataValue() {
        Mockito.`when`(res.status).thenReturn(200)
        Mockito.`when`(res.getHeader("Content-Length")).thenReturn("1024")
        val info = ServletHttpResponseInfo(res)
        Truth.assertThat(info.tryGetMetadata(HttpResponseMetadataKey.StatusCode)).isInstanceOf(Integer::class.javaObjectType)
        Truth.assertThat(info.tryGetMetadata(HttpResponseMetadataKey.StatusCode)).isEqualTo(200)
        Truth.assertThat(info.tryGetMetadata(HttpResponseMetadataKey.ContentLength)).isInstanceOf(Long::class.javaObjectType)
        Truth.assertThat(info.tryGetMetadata(HttpResponseMetadataKey.ContentLength)).isEqualTo(1024L)
    }

    @Test
    fun testMetadataValueOldVersion() {
        val info = ServletHttpResponseInfo(res, true)
        Truth.assertThat(info.tryGetMetadata(HttpResponseMetadataKey.StatusCode)).isNull()
        Truth.assertThat(info.tryGetMetadata(HttpResponseMetadataKey.ContentLength)).isNull()
    }
}
