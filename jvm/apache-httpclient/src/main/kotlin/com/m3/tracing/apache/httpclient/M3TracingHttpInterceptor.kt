package com.m3.tracing.apache.httpclient

import com.m3.tracing.M3Tracer
import com.m3.tracing.M3TracerFactory
import com.m3.tracing.TraceSpan
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.HttpResponse
import org.apache.http.HttpResponseInterceptor
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.protocol.HttpContext
import org.slf4j.LoggerFactory

/**
 * Interceptor for Apache HTTP Client.
 *
 */
open class M3TracingHttpInterceptor(
        protected val tracer: M3Tracer
) : HttpRequestInterceptor, HttpResponseInterceptor {
    companion object {
        @JvmStatic
        public val INSTANCE = M3TracingHttpInterceptor()

        private val currentSpan = ThreadLocal<TraceSpan>()
        private val logger = LoggerFactory.getLogger(M3TracingHttpInterceptor::class.java)
    }

    constructor(): this(M3TracerFactory.get())

    override fun process(request: HttpRequest, context: HttpContext) {
        val span = tracer.startSpan(createSpanName(request))
        currentSpan.set(span) // Set to ThreadLocal ASAP to prevent leak

        doQuietly {
            span["method"] = request.requestLine.method
            span["uri"] = request.requestLine.uri
        }
    }

    override fun process(response: HttpResponse, context: HttpContext) {
        val span = currentSpan.get() ?: return
        currentSpan.set(null) // Prevent ClassLoader leak

        // Must continue to span.close() statement to prevent memory leak
        doQuietly {
            span["status"] = response.statusLine.statusCode
        }
        span.close()
    }


    private fun createSpanName(request: HttpRequest): String {
        // Intentionally excluded queryString because it might contain dynamic string
        // Dynamic span name makes runningSpan table so huge
        return if (request is HttpUriRequest) {
            "HTTP ${request.method} ${request.uri.host}"
        } else {
            "HTTP ${request.requestLine.method}"
        }
    }

    /**
     * `On Error Resume Next` in 21st century.
     */
    protected fun doQuietly(action: () -> Unit) {
        try {
            action()
        } catch (e: Throwable) {
            logger.error("Failed to update Span.", e)
        }
    }
}