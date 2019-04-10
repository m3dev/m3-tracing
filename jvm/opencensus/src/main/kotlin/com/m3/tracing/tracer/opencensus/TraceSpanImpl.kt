package com.m3.tracing.tracer.opencensus

import com.m3.tracing.TraceSpan
import io.opencensus.trace.EndSpanOptions
import io.opencensus.trace.Span

abstract class TraceSpanImpl: TraceSpan {
    protected abstract val span: Span

    override fun close() {
        span.end(EndSpanOptions.DEFAULT)
    }
}
