package com.m3.tracing.http

import com.m3.tracing.TraceSpan

/**
 * Represents an incoming & running HTTP request.
 */
interface HttpRequestSpan: TraceSpan {

    /**
     * Should set this property if sending/sent HTTP response.
     */
    fun setResponse(response: HttpResponseInfo)
}
