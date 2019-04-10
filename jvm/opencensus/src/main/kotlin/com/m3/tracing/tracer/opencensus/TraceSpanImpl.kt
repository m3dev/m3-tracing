package com.m3.tracing.tracer.opencensus

import com.m3.tracing.NoopSpan
import com.m3.tracing.TraceSpan
import io.grpc.Context
import io.opencensus.common.Scope
import io.opencensus.trace.AttributeValue
import io.opencensus.trace.EndSpanOptions
import io.opencensus.trace.Span
import io.opencensus.trace.Tracer
import org.slf4j.LoggerFactory


abstract class TraceSpanImpl(
        private val parentSpan: TraceSpanImpl?
): TraceSpan {
    companion object {
        private val logger = LoggerFactory.getLogger(TraceSpanImpl::class.java)
    }

    protected abstract val tracer: Tracer
    protected abstract val span: Span
    /** OpenCensus Scope bound to the span. */
    protected abstract val scope: Scope?
    /** OpenCensus context (gRPC context) bound to this trace/span. */
    protected abstract val grpcContext: Context
    /** If not null, call `detach(grpcContextDetachTo)` in the end of this span. */
    protected abstract val grpcContextDetachTo: Context?

    private val threadID = Thread.currentThread().id

    override fun close() {
        closeQuietly { scope?.close() } // note: Scope.close not always closes span. Should close span also.
        closeQuietly { span.end(EndSpanOptions.DEFAULT) }
        closeQuietly { if (grpcContextDetachTo != null) { grpcContext.detach(grpcContextDetachTo) } }
    }

    override fun startChildSpan(name: String): TraceSpan {
        try {
            val grpcContextDetachTo = if (parentSpan != null && parentSpan.threadID != this.threadID) {
                // Need to attach gRPC context to propagate OpenCensus context from parent thread
                // see: https://github.com/GoogleCloudPlatform/cloud-trace-java/issues/85#issuecomment-440402234
                grpcContext.attach()
            } else {
                null
            }
            val span = tracer.spanBuilderWithExplicitParent(name, this.span).startSpan()
            val scope = tracer.withSpan(span)
            return NonRootSpan(
                    parentSpan = this,
                    span = span,
                    scope = scope,
                    grpcContextDetachTo = grpcContextDetachTo
            )
        } catch (e: Throwable) {
            logger.error("Failed to startChildSpan", e)
            return NoopSpan
        }
    }

    protected fun closeQuietly(action: () -> Unit) {
        try {
            action()
        } catch (e: Throwable) {
            logger.error("Failed to cleanup tracing. Might cause memory leak.", e)
        }
    }

    override fun set(tagName: String, value: String?): TraceSpan { if (value != null) span.putAttribute(tagName, AttributeValue.stringAttributeValue(value)); return this }
    override fun set(tagName: String, value: Boolean?): TraceSpan { if (value != null) span.putAttribute(tagName, AttributeValue.booleanAttributeValue(value)); return this }
    override fun set(tagName: String, value: Int?): TraceSpan { if (value != null) span.putAttribute(tagName, AttributeValue.longAttributeValue(value.toLong())); return this }
    override fun set(tagName: String, value: Long?): TraceSpan { if (value != null) span.putAttribute(tagName, AttributeValue.longAttributeValue(value)); return this }

    class NonRootSpan(
            parentSpan: TraceSpanImpl,
            override val span: Span,
            override val scope: Scope?,
            override val grpcContextDetachTo: Context?
    ): TraceSpanImpl(parentSpan) {
        override val tracer: Tracer = parentSpan.tracer
        override val grpcContext: Context = parentSpan.grpcContext
    }
}
