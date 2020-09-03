package com.m3.tracing.apache.httpclient

import org.apache.http.client.HttpRequestRetryHandler
import org.apache.http.protocol.HttpContext
import org.slf4j.LoggerFactory
import java.io.IOException

open class M3TracingHttpRequestRetryHandler(private val baseHandler: HttpRequestRetryHandler) : HttpRequestRetryHandler {
    companion object {
        private val logger = LoggerFactory.getLogger(M3TracingHttpRequestRetryHandler::class.java)
    }

    override fun retryRequest(exception: IOException?, executionCount: Int, context: HttpContext?): Boolean {
        val result = baseHandler.retryRequest(exception, executionCount, context)

        // When not retrying, try to close current span
        if(!result) {
            M3TracingHttpInterceptor.INSTANCE.closeSpan {
                it["error"] = exception?.message
            }
        }
        return result
    }
}
