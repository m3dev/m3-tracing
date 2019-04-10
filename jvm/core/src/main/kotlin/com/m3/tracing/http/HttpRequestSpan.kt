package com.m3.tracing.http

import com.m3.tracing.TraceSpan

/**
 * Represents an incoming & running HTTP request.
 */
interface HttpRequestSpan: TraceSpan {
    /**
     * If exception raised while processing this request, should set this property.
     */
    fun setError(e: Throwable?)

    /**
     * Should set this property if sending/sent HTTP response.
     */
    fun setResponse(response: HttpResponseInfo)
}
