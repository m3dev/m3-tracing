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
interface M3Tracer: AutoCloseable {

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
     * Start a new span in current thread / call stack.
     * Call this method before your logic and call [TraceSpan.close] in the end of your logic.
     * MUST close resulted span in the end of your logic.
     * Consider to use try-with-resources (Java) or `use` method (Kotlin).
     *
     * If your logic run in child thread / worker thread, call this method within the child thread (not in the parent thread).
     *
     * This method creates child span if there is a running span in this thread (behave same as [TraceSpan.startChildSpan]).
     */
    @CheckReturnValue // Caller must close the new span
    fun startSpan(name: String): TraceSpan
}
