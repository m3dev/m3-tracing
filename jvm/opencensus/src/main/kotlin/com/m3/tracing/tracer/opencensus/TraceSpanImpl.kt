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


/** When creating this instance, must call [init] method also. */
internal abstract class TraceSpanImpl(
        private val parentSpan: TraceSpanImpl?
): TraceSpan {
    companion object {
        private val logger = LoggerFactory.getLogger(TraceSpanImpl::class.java)

        private val currentSpanKey = Context.key<TraceSpanImpl>("${TraceSpanImpl::class.java.name}#current")

        /** Get current TraceSpan (of current thread / call stack) */
        internal fun getCurrent(): TraceSpanImpl? = currentSpanKey.get()
    }

    protected abstract val tracer: Tracer
    protected abstract val span: Span
    /** OpenCensus Scope bound to the span. Creator of this object must enter to the scope. */
    protected abstract val scope: Scope?
    /** OpenCensus context (OpenCensuse uses gRPC context) bound to this trace/span. This context must be same with [Context.current()] during this span. */
    protected abstract val grpcContext: Context
    /** If not null, call `detach(grpcContextDetachTo)` in the end of this span. */
    protected abstract val grpcContextDetachTo: Context?

    /** To set [currentSpanKey], this TraceSpan also creates "inner" context. */
    private lateinit var innerContext: Context

    private val threadID = Thread.currentThread().id

    fun init() {
        innerContext = grpcContext.withValue(currentSpanKey, this)
        if (innerContext.attach() != grpcContext) {
            // grpcContext should be current context.
            // Because Context#attach() returns current context, this mismatch means bug.
            logger.error("(Potential bug) grpcContext != current context, it means caller of TraceSpanImpl did not set grpcContext properly.")
        }
    }

    override fun close() {
        closeQuietly { innerContext.detach(grpcContext) }
        closeQuietly { scope?.close() } // note: Scope.close not always closes span. Should close span also.
        closeQuietly { span.end(EndSpanOptions.DEFAULT) }
        closeQuietly { if (grpcContextDetachTo != null) { grpcContext.detach(grpcContextDetachTo) } }
    }

    override fun startChildSpan(name: String): TraceSpan {
        try {
            val grpcContextDetachTo = if (parentSpan != null && parentSpan.threadID != this.threadID) {
                // Need to attach gRPC context to propagate OpenCensus context from parent thread
                // see: https://github.com/GoogleCloudPlatform/cloud-trace-java/issues/85#issuecomment-440402234
                //
                // This attach() call must BEFORE span creation.
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
            ).also {
                it.init()
            }
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
            // Note should not inherit parent.context.
            // Because each scope makes new gRPC context, each span/scope bound to different context.
            override val grpcContext: Context = Context.current(),
            override val grpcContextDetachTo: Context?
    ): TraceSpanImpl(parentSpan) {
        override val tracer: Tracer = parentSpan.tracer
    }

    class RootSpan(
            override val tracer: Tracer,
            override val span: Span,
            override val scope: Scope,
            override val grpcContext: Context = Context.current()
    ): TraceSpanImpl(null){
        override val grpcContextDetachTo = null
    }
}
