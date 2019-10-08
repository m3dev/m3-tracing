package com.m3.tracing.tracer.opencensus

import com.google.common.annotations.VisibleForTesting
import com.m3.tracing.http.*
import io.grpc.Context
import io.opencensus.common.Scope
import io.opencensus.contrib.http.HttpClientHandler
import io.opencensus.contrib.http.HttpServerHandler
import io.opencensus.trace.Tracer
import io.opencensus.trace.propagation.TextFormat
import org.slf4j.LoggerFactory

internal class HttpRequestTracer(
        private val tracer: Tracer,
        private val textFormat: TextFormat,
        private val publicEndpoint: Boolean
) {
    @VisibleForTesting
    internal val getter = object: TextFormat.Getter<HttpRequestInfo>() {
        override fun get(carrier: HttpRequestInfo, key: String): String? {
            return carrier.tryGetHeader(key)
        }
    }
    @VisibleForTesting
    internal val setter = object: TextFormat.Setter<HttpRequestInfo>() {
        override fun put(carrier: HttpRequestInfo, key: String, value: String) {
            return carrier.trySetHeader(key, value)
        }
    }
    @VisibleForTesting
    internal val extractor = ExtractorImpl()
    @VisibleForTesting
    internal val handler = HttpServerHandler(tracer, extractor, textFormat, getter, publicEndpoint)
    @VisibleForTesting
    internal val clientHandler = HttpClientHandler(tracer, extractor, textFormat, setter)

    fun processRequest(request: HttpRequestInfo) = HttpRequestSpanImpl(handler, tracer, request).also {
        it.init()
    }

    fun processClientRequest(request: HttpRequestInfo) = HttpClientRequestSpanImpl(clientHandler, tracer, request).also {
        it.init()
    }
}

internal class HttpRequestSpanImpl(
        private val handler: HttpServerHandler<HttpRequestInfo, HttpResponseInfo, HttpRequestInfo>,
        override val tracer: Tracer,
        private val request: HttpRequestInfo
): TraceSpanImpl(null), HttpRequestSpan {
    companion object {
        private val requestHeadersToCapture = listOf("X-Forwarded-For")
        private val logger = LoggerFactory.getLogger(HttpRequestTracer::class.java)
    }

    val context = handler.handleStart(request, request).also { context ->
        request.tryGetMetadata(HttpRequestMetadataKey.ContentLength)?.let { length ->
            if (length > 0) handler.handleMessageReceived(context, length)
        }
    }

    override val scopeParentContext: Context? get() = null
    override val span = handler.getSpanFromContext(context)
    override val scope: Scope? = tracer.withSpan(span)

    @VisibleForTesting
    internal var error: Throwable? = null
    override fun setError(e: Throwable?) {
        super<TraceSpanImpl>.setError(e)
        this.error = e
    }

    @VisibleForTesting
    internal var response: HttpResponseInfo? = null
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

        // Must close scope, span in ANY case to prevent memory leak.
        // So that MUST call super.close().
        super.close()
    }

    private fun captureInfo() {
        response?.tryGetMetadata(HttpResponseMetadataKey.ContentLength)?.let { length ->
            handler.handleMessageSent(context, length)
        }
        requestHeadersToCapture.forEach { header ->
            span.putAttribute(header, request.tryGetHeader(header))
        }
    }
}

internal class HttpClientRequestSpanImpl(
        private val handler: HttpClientHandler<HttpRequestInfo, HttpResponseInfo, HttpRequestInfo>,
        override val tracer: Tracer,
        private val request: HttpRequestInfo
): TraceSpanImpl(null), HttpRequestSpan {
    companion object {
        private val logger = LoggerFactory.getLogger(HttpRequestTracer::class.java)
    }

    val context = handler.handleStart(tracer.currentSpan, request, request).also { context ->
        request.tryGetMetadata(HttpRequestMetadataKey.ContentLength)?.let { length ->
            if (length > 0) handler.handleMessageSent(context, length)
        }
    }

    override val scopeParentContext: Context? get() = null
    override val span = handler.getSpanFromContext(context)
    override val scope: Scope? = tracer.withSpan(span)

    @VisibleForTesting
    internal var error: Throwable? = null
    override fun setError(e: Throwable?) {
        super<TraceSpanImpl>.setError(e)
        this.error = e
    }

    @VisibleForTesting
    internal var response: HttpResponseInfo? = null
    override fun setResponse(response: HttpResponseInfo) {
        this.response = response
    }

    override fun close() {
        try {
            captureInfo()
            handler.handleEnd(context, request, response, error)
        } catch (e: Throwable) {
            logger.error("Failed to capture client response detail", e)
        }

        // Must close scope, span in ANY case to prevent memory leak.
        // So that MUST call super.close().
        super.close()
    }

    private fun captureInfo() {
        response?.tryGetMetadata(HttpResponseMetadataKey.ContentLength)?.let { length ->
            handler.handleMessageReceived(context, length)
        }
    }
}

