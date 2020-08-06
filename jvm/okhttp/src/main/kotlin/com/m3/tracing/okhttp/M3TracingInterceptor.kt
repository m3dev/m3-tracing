package com.m3.tracing.okhttp

import com.m3.tracing.M3Tracer
import com.m3.tracing.M3TracerFactory
import com.m3.tracing.http.HttpRequestMetadataKey
import okhttp3.Interceptor
import okhttp3.Response
import org.slf4j.LoggerFactory

/**
 * Interceptor for Apache HTTP Client.
 *
 */
open class M3TracingInterceptor(
        private val tracer: M3Tracer
) : Interceptor {
    companion object {
        private val logger = LoggerFactory.getLogger(M3TracingInterceptor::class.java)
    }

    constructor() : this(M3TracerFactory.get())

    override fun intercept(chain: Interceptor.Chain): Response {
        val requestInfo = MutableHttpRequestInfo(chain.request())
        val span = tracer.processOutgoingHttpRequest(requestInfo)

        doQuietly {
            span["client"] = "m3-tracing:okhtp"
            span["method"] = requestInfo.tryGetMetadata(HttpRequestMetadataKey.Method)
            span["path"] = requestInfo.tryGetMetadata(HttpRequestMetadataKey.Path)
        }

        val response = chain.proceed(requestInfo.build())

        doQuietly {
            span["status"] = response.code
        }

        span.close()

        return response
    }

    private fun doQuietly(action: () -> Unit) {
        try {
            action()
        } catch (e: Throwable) {
            logger.error("Failed to update Span.", e)
        }
    }
}
