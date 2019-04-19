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
        internal fun getCurrent(context: Context): TraceSpanImpl? = currentSpanKey.get(context)

        internal fun closeQuietly(action: () -> Unit) {
            try {
                action()
            } catch (e: Throwable) {
                logger.error("Failed to cleanup tracing. Might cause memory leak.", e)
            }
        }
    }

    protected abstract val tracer: Tracer
    protected abstract val span: Span

    /** OpenCensus Scope bound to the span. Creator of this object must enter to the scope.
     * Note that OpenCensuse [Scope] itself _MAY_ create gRPC context, or may not.
     */
    protected abstract val scope: Scope?
    /** gRPC context that is parent of gRPC context of the span. If this is given, close it on the end of this span. */
    protected abstract val scopeParentContext: Context?

    /** To set [currentSpanKey], this TraceSpan also creates "inner" context. */
    private lateinit var innerContext: Context
    /** gRPC context that is parent of [innerContext] */
    private lateinit var innerContextParentContext: Context

    private val threadID = Thread.currentThread().id

    fun init() {
        innerContext = Context.current().withValue(currentSpanKey, this)
        innerContextParentContext = innerContext.attach()
    }

    override fun close() {
        closeQuietly { innerContext.detach(innerContextParentContext) }
        closeQuietly { scope?.close() } // note: Scope.close not always closes span. Should close span also.
        closeQuietly { span.end(EndSpanOptions.DEFAULT) }
        closeQuietly { if (scopeParentContext != null) { Context.current().detach(scopeParentContext) } }
    }

    override fun startChildSpan(name: String): TraceSpan {
        try {
            val scopeParentContext = if (this.threadID != Thread.currentThread().id) {
                // Need to attach gRPC context to propagate OpenCensus context from parent thread
                // see: https://github.com/GoogleCloudPlatform/cloud-trace-java/issues/85#issuecomment-440402234
                //
                // This attach() call must BEFORE span creation.
                innerContext.attach()
            } else {
                null
            }
            val span = tracer.spanBuilderWithExplicitParent(name, this.span).startSpan()
            val scope = tracer.withSpan(span)
            return NonRootSpan(
                    parentSpan = this,
                    span = span,
                    scope = scope,
                    scopeParentContext = scopeParentContext
            ).also {
                it.init()
            }
        } catch (e: Throwable) {
            logger.error("Failed to startChildSpan", e)
            return NoopSpan
        }
    }

    override fun set(tagName: String, value: String?): TraceSpan { if (value != null) span.putAttribute(tagName, AttributeValue.stringAttributeValue(value)); return this }
    override fun set(tagName: String, value: Boolean?): TraceSpan { if (value != null) span.putAttribute(tagName, AttributeValue.booleanAttributeValue(value)); return this }
    override fun set(tagName: String, value: Long?): TraceSpan { if (value != null) span.putAttribute(tagName, AttributeValue.longAttributeValue(value)); return this }

    class NonRootSpan(
            parentSpan: TraceSpanImpl,
            override val span: Span,
            override val scope: Scope?,
            override val scopeParentContext: Context?
    ): TraceSpanImpl(parentSpan) {
        override val tracer: Tracer = parentSpan.tracer
    }
}
