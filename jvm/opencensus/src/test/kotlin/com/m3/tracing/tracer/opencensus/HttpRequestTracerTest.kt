package com.m3.tracing.tracer.opencensus

import com.google.common.truth.Truth
import com.m3.tracing.http.HttpRequestInfo
import com.m3.tracing.http.HttpResponseInfo
import io.opencensus.contrib.http.HttpServerHandler
import io.opencensus.trace.Span
import io.opencensus.trace.Tracer
import io.opencensus.trace.propagation.TextFormat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

class HttpRequestTracerTest {

    @ExtendWith(MockitoExtension::class)
    class `Internal objects` {

        @Mock
        lateinit var tracer: Tracer
        @Mock
        lateinit var textFormat: TextFormat
        @Mock
        lateinit var httpRequest: HttpRequestInfo

        private val httpRequestTracer by lazy { HttpRequestTracer(tracer, textFormat, true) }

        @Test
        fun `Should pass-through header`() {
            Mockito.`when`(httpRequest.tryGetHeader("header")).thenReturn("value of header")
            Mockito.`when`(httpRequest.tryGetHeader("not_found_header")).thenReturn(null)

            Truth.assertThat(httpRequestTracer.getter.get(httpRequest, "header")).isEqualTo("value of header")
            Truth.assertThat(httpRequestTracer.getter.get(httpRequest, "not_found_header")).isNull()
        }
    }

    @ExtendWith(MockitoExtension::class)
    class `Test simple methods of HttpRequestSpanImpl` {
        @Mock
        lateinit var handler: HttpServerHandler<HttpRequestInfo, HttpResponseInfo, HttpRequestInfo>
        @Mock
        lateinit var tracer: Tracer
        @Mock
        lateinit var httpRequest: HttpRequestInfo
        @Mock
        lateinit var span: Span

        private val httpRequestSpanImpl by lazy { HttpRequestSpanImpl(handler, tracer, httpRequest) }

        @BeforeEach
        fun setupMocks() {
            Mockito.`when`(handler.getSpanFromContext(Mockito.any())).thenReturn(span)
        }

        @Test
        fun testSetError() {
            val e = RuntimeException("test exception")
            httpRequestSpanImpl.setError(e)

            Truth.assertThat(httpRequestSpanImpl.error).isEqualTo(e)
            Mockito.verify(span).putAttribute("exception_class", RuntimeException::class.java.name)
            Mockito.verify(span).putAttribute("exception_messsage", "test exception")
        }

        @Test
        fun testSetResponse() {
            Truth.assertThat(httpRequestSpanImpl.response).isNull()

        }
    }
}