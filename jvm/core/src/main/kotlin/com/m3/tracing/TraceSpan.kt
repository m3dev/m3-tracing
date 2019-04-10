package com.m3.tracing

interface TraceSpan: AutoCloseable {

    /**
     * Close this span, means end of this span.
     */
    @Throws() // Remove checked exception
    override fun close()
}