package com.m3.tracing.apache.httpclient

import com.m3.tracing.M3Tracer
import com.m3.tracing.M3TracerFactory
import com.m3.tracing.TraceSpan
import com.m3.tracing.http.HttpRequestMetadataKey
import org.apache.http.HttpRequest
import org.apache.http.HttpRequestInterceptor
import org.apache.http.HttpResponse
import org.apache.http.HttpResponseInterceptor
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
        @JvmField
        public val INSTANCE = M3TracingHttpInterceptor()

        private val currentSpan = ThreadLocal<TraceSpan>()
        private val logger = LoggerFactory.getLogger(M3TracingHttpInterceptor::class.java)
    }

    constructor() : this(M3TracerFactory.get())

    internal fun getCurrentSpan() : TraceSpan? {
        return currentSpan.get()
    }

    internal fun resetCurrentSpan() {
        currentSpan.set(null) // Prevent ClassLoader leak
    }

    override fun process(request: HttpRequest, context: HttpContext) {
        val requestInfo = ApacheHttpRequestInfo(request, context)
        val span = tracer.processOutgoingHttpRequest(requestInfo)
        currentSpan.set(span) // Set to ThreadLocal ASAP to prevent leak

        doQuietly {
            span["client"] = "m3-tracing:apache-httpclient"
            span["method"] = requestInfo.tryGetMetadata(HttpRequestMetadataKey.Method)
            span["path"] = requestInfo.tryGetMetadata(HttpRequestMetadataKey.Path)
        }
    }

    override fun process(response: HttpResponse, context: HttpContext) {
        val span = currentSpan.get()
        if(span == null) {
            logger.warn("Current span is not found. Unable to close and send span.")
            return
        }

        currentSpan.set(null) // Prevent ClassLoader leak

        // Must continue to span.close() statement to prevent memory leak
        doQuietly {
            span["status"] = response.statusLine.statusCode
        }
        span.close()
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
