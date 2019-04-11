package com.m3.tracing.tracer.logging

import com.m3.tracing.M3Tracer
import com.m3.tracing.TraceSpan
import com.m3.tracing.http.HttpRequestInfo
import com.m3.tracing.http.HttpRequestSpan
import com.m3.tracing.http.HttpResponseInfo
import org.slf4j.LoggerFactory

/**
 * Just output trace into log.
 * Useful for local development / unittest.
 */
class M3LoggingTracer: M3Tracer {
    companion object {
        private val output = LoggerFactory.getLogger(M3LoggingTracer::class.java)
    }

    override fun close() {
        output.info("Tracer closed")
    }

    override fun processIncomingHttpRequest(request: HttpRequestInfo): HttpRequestSpan = object: TraceSpanImpl("HTTP ${request.url}"), HttpRequestSpan {
        private var e: Throwable? = null
        override fun setError(e: Throwable?) {
            this.e = e
        }

        override fun setResponse(response: HttpResponseInfo) {}
        override fun close() {
            if (this.e != null) {
                output.info("Error captured in request ${request.url}: $e")
            }
        }

        override fun startChildSpan(name: String): TraceSpan = TraceSpanImpl(name)
    }

    override fun startSpan(name: String): TraceSpan = TraceSpanImpl(name)

    private open class TraceSpanImpl(protected val name: String): TraceSpan {
        init {
            output.info("Start of Tracing Span: \"$name\"")
        }
        override fun close() {
            output.info("End of Tracing Span: \"$name\" (${tags.filterValues { it != null }})")
        }

        override fun startChildSpan(name: String): TraceSpan {
            return TraceSpanImpl(name)
        }

        private val tags = mutableMapOf<String, Any?>()
        override fun set(tagName: String, value: String?): TraceSpan { tags[tagName] = value; return this }
        override fun set(tagName: String, value: Boolean?): TraceSpan { tags[tagName] = value; return this }
        override fun set(tagName: String, value: Int?): TraceSpan { tags[tagName] = value; return this }
        override fun set(tagName: String, value: Long?): TraceSpan { tags[tagName] = value; return this }
    }
}
