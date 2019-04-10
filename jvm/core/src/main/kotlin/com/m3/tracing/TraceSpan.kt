package com.m3.tracing

import javax.annotation.CheckReturnValue

interface TraceSpan: AutoCloseable {

    /**
     * Close this span, means end of this span.
     */
    @Throws() // Remove checked exception
    override fun close()

    /**
     * Create child span.
     *
     * If this span run in different thread from parent span, must call this method from child thread.
     *
     * Multi-thread example:
     *
     * ```java
     * TraceSpan parentSpan;
     * Executor executor;
     *
     * executor.execute {
     *    // childSpan() method must be called in worker thread (not outside of executor).
     *    try(TraceSpan span = parentSpan.startChildSpan("do_something")){
     *        // ... do something ...
     *    }
     * }
     * ```
     *
     * @return Child span. Do NOT forget to close resulted span.
     */
    @CheckReturnValue
    fun startChildSpan(name: String): TraceSpan

    /** Set tag/attribute into this span. */
    fun set(tagName: String, value: String?): TraceSpan
    /** Set tag/attribute into this span. */
    fun set(tagName: String, value: Boolean?): TraceSpan
    /** Set tag/attribute into this span. */
    fun set(tagName: String, value: Int?): TraceSpan
    /** Set tag/attribute into this span. */
    fun set(tagName: String, value: Long?): TraceSpan
}
