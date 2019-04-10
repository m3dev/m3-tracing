package com.m3.tracing.tracer.opencensus

import com.m3.tracing.http.*
import io.opencensus.contrib.http.HttpServerHandler
import io.opencensus.trace.Tracer
import io.opencensus.trace.propagation.TextFormat
import org.slf4j.LoggerFactory

internal class HttpRequestTracer(
        private val tracer: Tracer,
        private val textFormat: TextFormat
) {
    private val getter = object: TextFormat.Getter<HttpRequestInfo>() {
        override fun get(carrier: HttpRequestInfo, key: String): String? {
            return carrier.tryGetHeader(key)
        }
    }
    private val extractor = ExtractorImpl()
    private val handler = HttpServerHandler(tracer, extractor, textFormat, getter, true)

    fun processRequest(request: HttpRequestInfo) = HttpRequestSpanImpl(handler, tracer, request)
}

internal class HttpRequestSpanImpl(
        private val handler: HttpServerHandler<HttpRequestInfo, HttpResponseInfo, HttpRequestInfo>,
        tracer: Tracer,
        private val request: HttpRequestInfo
): TraceSpanImpl(), HttpRequestSpan {
    companion object {
        private val requestHeadersToCapture = listOf("X-Forwarded-For")
        private val logger = LoggerFactory.getLogger(HttpRequestTracer::class.java)
    }

    val context = handler.handleStart(request, request).also { context ->
        request.tryGetMetadata(HttpRequestMetadataKey.ContentLength)?.let { length ->
            if (length > 0) handler.handleMessageReceived(context, length)
        }
    }

    override val span = handler.getSpanFromContext(context)
    val scope = tracer.withSpan(span)

    private var error: Throwable? = null
    override fun setError(e: Throwable?) {
        if (e != null) {
            this.error = e
            span.saveException(e)
        }
    }

    private var response: HttpResponseInfo? = null
    override fun setResponse(response: HttpResponseInfo) {
        this.response = response
    }

    override fun close() {
        try {
            captureInfo()
            handler.handleEnd(context, request, response, error)
        } catch (e: Throwable) {
            logger.error("Failed to capture response detail", e)
        }

        // Must close scope, span in ANY case to prevent memory leak
        closeQuietly { scope.close() }
        closeQuietly { super.close() } // Calls span.end()
    }

    private fun captureInfo() {
        response?.tryGetMetadata(HttpResponseMetadataKey.ContentLength)?.let { length ->
            handler.handleMessageSent(context, length)
        }
        requestHeadersToCapture.forEach { header ->
            span.putAttribute(header, request.tryGetHeader(header))
        }
    }

    private fun closeQuietly(action: () -> Unit) {
        try {
            action()
        } catch (e: Throwable) {
            logger.error("Failed to cleanup tracing. Might cause memory leak.", e)
        }
    }
}
