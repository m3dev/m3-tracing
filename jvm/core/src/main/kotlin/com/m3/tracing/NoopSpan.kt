package com.m3.tracing

/**
 * Span does not do anything.
 * Mostly used for fall-back on unexpected error.
 */
object NoopSpan: TraceSpan {
    override fun startChildSpan(name: String) = NoopSpan
    override fun close() {}

    override fun set(tagName: String, value: String?): TraceSpan { return this }
    override fun set(tagName: String, value: Boolean?): TraceSpan { return this }
    override fun set(tagName: String, value: Long?): TraceSpan { return this }
}
