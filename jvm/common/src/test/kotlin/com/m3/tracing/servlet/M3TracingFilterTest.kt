package com.m3.tracing.servlet

import io.opencensus.exporter.trace.logging.LoggingTraceExporter
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@ExtendWith(MockitoExtension::class)
class M3TracingFilterTest {
    companion object {
        @JvmStatic @AfterAll
        fun afterAll() {
            Thread.sleep(5 * 1000) // Wait until export (export batch runs every 5sec)
        }
    }

    @Mock lateinit var request: HttpServletRequest
    @Mock lateinit var response: HttpServletResponse
    @Mock lateinit var chain: FilterChain
    @Mock lateinit var filterConfig: FilterConfig
    val filter = M3TracingFilter()

    @BeforeEach
    fun beforeEach() {
        Mockito.`when`(request.requestURL).thenReturn(StringBuffer("http://example.com/test/"))
        Mockito.`when`(filterConfig.getInitParameter("shutdown_tracer")).thenReturn("false")
        Mockito.`when`(filterConfig.getInitParameter("sampling_ratio")).thenReturn("always")

        LoggingTraceExporter.register()
    }

    @AfterEach
    fun afterEach() {
        filter.destroy()
    }

    @Test
    fun chainMustBeCalledEvenIfFilterFailedBeforeChain() {
        Mockito.`when`(request.contentLength).thenThrow(RuntimeException("test exception"))

        filter.init(filterConfig)
        filter.doFilter(request, response, chain)

        Mockito.verify(chain).doFilter(request, response)
    }

    @Test
    fun filterShouldNotConsumeRequestContent() {
        filter.init(filterConfig)
        filter.doFilter(request, response, chain)

        // Filter should not call those methods because application logic may call setCharacterEncoding.
        // If filter calls those methods before, setCharacterEncoding silently fails.
        Mockito.verify(request, Mockito.never()).characterEncoding = Mockito.any()
        Mockito.verify(request, Mockito.never()).getParameter(Mockito.any())
        Mockito.verify(request, Mockito.never()).reader
        Mockito.verify(request, Mockito.never()).inputStream
        Mockito.verify(request, Mockito.never()).queryString // https://stackoverflow.com/a/19409520/914786
    }
}