package com.m3.tracing.tracer.servlet

import com.google.common.truth.Truth
import com.m3.tracing.M3Tracer
import com.m3.tracing.http.HttpRequestSpan
import com.m3.tracing.tracer.opencensus.M3OpenCensusTracer
import com.nhaarman.mockitokotlin2.any
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import javax.servlet.FilterChain
import javax.servlet.FilterConfig
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


class M3TracingFilterTest {

    @ExtendWith(MockitoExtension::class)
    class `Test filterConfig` {
        @Mock lateinit var tracer: M3Tracer
        @Mock lateinit var filterConfig: FilterConfig
        private val filter by lazy { M3TracingFilter() }

        @Test
        fun `test shutdown_tracer is false`() {
            Mockito.`when`(filterConfig.getInitParameter("shutdown_tracer")).thenReturn("false")

            filter.init(filterConfig)
            Truth.assertThat(filter.config.shutdownTracer).isFalse()
        }

        @Test
        fun `test shutdown_tracer is true`() {
            Mockito.`when`(filterConfig.getInitParameter("shutdown_tracer")).thenReturn("true")

            filter.init(filterConfig)
            Truth.assertThat(filter.config.shutdownTracer).isTrue()
        }

        @Test
        fun `should ignore double init`() {
            filter.init(M3TracingFilter.Config(tracer, false))

            filter.init(filterConfig)
            Mockito.verify(filterConfig, Mockito.never()).getInitParameter(any())

            filter.init(M3TracingFilter.Config(tracer, true))
            Truth.assertThat(filter.config.shutdownTracer).isFalse()
        }
    }

    @ExtendWith(MockitoExtension::class)
    class `Test destroy`() {
        @Mock lateinit var tracer: M3Tracer
        private val filter by lazy { M3TracingFilter() }

        @Test
        fun `close tracer if true`() {
            filter.init(M3TracingFilter.Config(tracer, true))
            filter.destroy()

            Mockito.verify(tracer).close()
        }

        @Test
        fun `Do not close tracer if false`() {
            filter.init(M3TracingFilter.Config(tracer, false))
            filter.destroy()

            Mockito.verify(tracer, Mockito.never()).close()
        }
    }

    @ExtendWith(MockitoExtension::class)
    class `Error handling test` {
        @Mock lateinit var tracer: M3Tracer
        @Mock lateinit var span: HttpRequestSpan
        @Mock lateinit var request: HttpServletRequest
        @Mock lateinit var response: HttpServletResponse
        @Mock lateinit var chain: FilterChain

        private val filter by lazy { M3TracingFilter() }

        @Test
        fun `FilterChain must be invoked if error occured before it`() {
            Mockito.`when`(tracer.processIncomingHttpRequest(any())).thenThrow(RuntimeException("Test exception"))

            filter.init(M3TracingFilter.Config(tracer, false))
            filter.doFilter(request, response, chain)

            Mockito.verify(chain, Mockito.only()).doFilter(request, response)
        }

        @Test
        fun `FilterChain should not be invoked if error occured within it`() {
            Mockito.`when`(tracer.processIncomingHttpRequest(any())).thenReturn(span)
            val e = RuntimeException("Test Exception")
            Mockito.`when`(chain.doFilter(any(), any())).thenThrow(e)

            var actualException: Throwable? = null
            filter.init(M3TracingFilter.Config(tracer, false))
            try {
                filter.doFilter(request, response, chain)
            } catch (e: Throwable) {
                actualException = e
            }

            Mockito.verify(chain, Mockito.only()).doFilter(request, response)
            Truth.assertThat(actualException).isSameInstanceAs(e)
        }

        @Test
        fun `FilterChain should not be invoked if error occured after it`() {
            Mockito.`when`(tracer.processIncomingHttpRequest(any())).thenReturn(span)
            Mockito.`when`(span.close()).thenThrow(RuntimeException("Test Exception"))

            filter.init(M3TracingFilter.Config(tracer, false))
            filter.doFilter(request, response, chain)

            Mockito.verify(chain, Mockito.only()).doFilter(request, response)
        }
    }

    @ExtendWith(MockitoExtension::class)
    class `Servlet spec issue` {
        @Mock lateinit var request: HttpServletRequest
        @Mock lateinit var response: HttpServletResponse
        @Mock lateinit var chain: FilterChain

        private val tracer by lazy { M3OpenCensusTracer() }
        private val filter by lazy { M3TracingFilter() }

        @AfterEach
        fun afterEach() {
            filter.destroy()
        }

        @Test
        fun filterShouldNotConsumeRequestContent() {
            filter.init(M3TracingFilter.Config(tracer, false))
            filter.doFilter(request, response, chain)

            Mockito.verify(request, Mockito.atLeastOnce()).getHeader(any()) // Ensure request is touched in this test case

            // Filter should not call those methods because application logic may call setCharacterEncoding.
            // If filter calls those methods before, setCharacterEncoding silently fails.
            Mockito.verify(request, Mockito.never()).characterEncoding = any()
            Mockito.verify(request, Mockito.never()).getParameter(any())
            Mockito.verify(request, Mockito.never()).reader
            Mockito.verify(request, Mockito.never()).inputStream
            Mockito.verify(request, Mockito.never()).queryString // https://stackoverflow.com/a/19409520/914786
        }
    }


    @ExtendWith(MockitoExtension::class)
    class `Edge case` {
        @Mock lateinit var req: ServletRequest
        @Mock lateinit var res: ServletResponse
        @Mock lateinit var chain: FilterChain

        private val filter by lazy { M3TracingFilter() }

        @Test
        fun `Should passthrough non http request`() {
            // Intentionally omitted init().
            // This filter should not do anything for non HTTP request.
            filter.doFilter(req, res, chain)

            Mockito.verify(chain).doFilter(req, res)
        }
    }
}
