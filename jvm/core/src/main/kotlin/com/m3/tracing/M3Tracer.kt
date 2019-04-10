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
}
