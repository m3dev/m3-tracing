package com.m3.tracing

import javax.annotation.CheckReturnValue
import javax.annotation.concurrent.ThreadSafe

/**
 * Context of tracing system.
 * With this object, you can pass context across threads.
 *
 * @see M3Tracer.currentContext
 */
@ThreadSafe
interface TraceContext {

    /**
     * Start a new span in current thread / call stack.
     * Call this method before your logic and call [TraceSpan.close] in the end of your logic.
     * MUST close resulted span in the end of your logic.
     * Consider to use try-with-resources (Java) or `use` method (Kotlin).
     */
    @CheckReturnValue // Caller must close the new span
    fun startSpan(name: String): TraceSpan
}