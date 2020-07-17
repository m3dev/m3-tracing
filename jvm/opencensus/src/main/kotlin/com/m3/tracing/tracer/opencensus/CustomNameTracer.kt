package com.m3.tracing.tracer.opencensus

import com.m3.tracing.internal.Config
import io.opencensus.trace.*
import org.slf4j.LoggerFactory

internal class CustomNameTracer(private val baseTracer: Tracer) : Tracer() {
    companion object {
        const val spanPrefixConfigName = "m3.tracer.span.prefix"

        private val logger = LoggerFactory.getLogger(CustomNameTracer::class.java)
    }

    private val spanPrefix: String? = Config[spanPrefixConfigName]

    constructor(): this(
            baseTracer = Tracing.getTracer()
    )
    {
        logger.debug("Tracer class is " + baseTracer.javaClass.name)
        // In most case, the type of baseTracer is io.opencensus.implcore.trace.TracerImpl which is in dependencies of this lib
        // If the type is NoopTrace, the result of tracing is not generated at all.
        // To avoid that, the project used this lib need to import a proper library which has a class implemented io.opencensus.trace.Tracing.
        if(baseTracer.javaClass.name == "io.opencensus.trace.Tracer\$NoopTracer")
        {
            logger.warn("The tracer becomes NoopTracer. Confirm your project has proper dependencies for activating tracing.")
        }
    }

    private fun generateSpanName(name: String?): String? {
        return if(spanPrefix.isNullOrBlank()) name else "[$spanPrefix] $name"
    }

    override fun spanBuilderWithExplicitParent(spanName: String?, parent: Span?): SpanBuilder? = baseTracer.spanBuilderWithExplicitParent(generateSpanName(spanName), parent)

    override fun spanBuilderWithRemoteParent(spanName: String?, remoteParentSpanContext: SpanContext?): SpanBuilder? = baseTracer.spanBuilderWithRemoteParent(generateSpanName(spanName), remoteParentSpanContext)
}