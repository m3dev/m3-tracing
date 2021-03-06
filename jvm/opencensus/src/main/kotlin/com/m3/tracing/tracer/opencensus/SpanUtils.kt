package com.m3.tracing.tracer.opencensus

import io.opencensus.trace.AttributeValue
import io.opencensus.trace.Span

internal fun Span.putAttribute(key: String, value: String?) {
    if (value == null) return
    this.putAttribute(key, AttributeValue.stringAttributeValue(value))
}
