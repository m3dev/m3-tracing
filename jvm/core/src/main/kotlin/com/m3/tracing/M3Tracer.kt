package com.m3.tracing

import com.m3.tracing.http.HttpRequestInfo
import com.m3.tracing.http.HttpRequestSpan
import javax.annotation.CheckReturnValue
import javax.annotation.concurrent.ThreadSafe

/**
 * Wrapper of distributed tracing library.
 * Instance of this interface can be obtained from [M3TracerFactory].
 *
 * Implementation requirements:
 * - Must be thread-safe
 * - Must have default public constructor
 */
@ThreadSafe
interface M3Tracer: AutoCloseable, TraceContext {

    /**
     * Shutdown tracer system.
     */
    @Throws() // Remove checked exception
    override fun close()

    /**
     * Start trace for incoming HTTP request.
     * Caller MUST close the [HttpRequestSpan] to prevent leak.
     */
    @CheckReturnValue
    fun processIncomingHttpRequest(request: HttpRequestInfo): HttpRequestSpan

    /**
     * Start trace for outgoing HTTP request.
     * Caller MUST close the [HttpRequestSpan] to prevent leak.
     */
    @CheckReturnValue
    fun processOutgoingHttpRequest(request: HttpRequestInfo): HttpRequestSpan

    /**
     * Returns context bound to current thread / call stack.
     *
     * When you get this object, it saves [TraceSpan] state of current thread / call stack.
     * With using `currentThreadContext.startSpan`, you can propagate span to another thread.
     */
    val currentContext: TraceContext

    /**
     * Start a new span in current thread / call stack.
     *
     * If your logic run across multiple threads, use [currentContext] rather than this.
     *
     * Call this method before your logic and call [TraceSpan.close] in the end of your logic.
     * MUST close resulted span in the end of your logic.
     * Consider to use try-with-resources (Java) or `use` method (Kotlin).
     */
    @JvmDefault
    override fun startSpan(name: String): TraceSpan = currentContext.startSpan(name)
}
