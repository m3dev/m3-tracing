package com.m3.tracing.tracer.logging

import com.m3.tracing.M3Tracer
import com.m3.tracing.TraceSpan
import com.m3.tracing.http.HttpRequestInfo
import com.m3.tracing.http.HttpRequestSpan
import com.m3.tracing.http.HttpResponseInfo
import org.slf4j.LoggerFactory

/**
 * Just output trace into log.
 */
class M3LoggingTracer: M3Tracer {
    private val output = LoggerFactory.getLogger(M3LoggingTracer::class.java)

    override fun close() {
        output.info("Tracer closed")
    }

    override fun processIncomingHttpRequest(request: HttpRequestInfo): HttpRequestSpan {
        output.info("Start of Request: ${request.url}")

        return object: TraceSpanImpl(), HttpRequestSpan {
            private var e: Throwable? = null
            private var response: HttpResponseInfo? = null

            override fun setError(e: Throwable?) {
                this.e = e
            }

            override fun setResponse(response: HttpResponseInfo) {
                this.response = response
            }

            override fun close() {
                if (this.e != null) {
                    output.info("Error captured in request ${request.url}: $e")
                }
                if (this.response != null) {
                    output.info("End of Request: ${request.url}")
                }
            }
        }
    }

    private open class TraceSpanImpl: TraceSpan {
        override fun close() {}
    }
}
