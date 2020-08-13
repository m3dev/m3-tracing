package com.m3.tracing.apache.httpclient

import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.protocol.HttpContext
import org.slf4j.LoggerFactory
import java.io.IOException

open class M3TracingHttpRequestRetryHandler() : DefaultHttpRequestRetryHandler() {
    companion object {
        @JvmField
        public val INSTANCE = M3TracingHttpRequestRetryHandler()

        private val logger = LoggerFactory.getLogger(M3TracingHttpRequestRetryHandler::class.java)
    }

    override fun retryRequest(exception: IOException?, executionCount: Int, context: HttpContext?): Boolean {
        val result = super.retryRequest(exception, executionCount, context)

        // When not retrying, try to close current span
        if(!result) {
            val span = M3TracingHttpInterceptor.INSTANCE.getCurrentSpan()
            if(span != null) {
                span["error"] = exception?.message
                M3TracingHttpInterceptor.INSTANCE.resetCurrentSpan()
                span.close()
            }
        }
        return result
    }
}
