package com.m3.tracing.tracer.servlet

import com.google.common.annotations.VisibleForTesting
import com.m3.tracing.M3Tracer
import com.m3.tracing.M3TracerFactory
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.annotation.concurrent.ThreadSafe
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Servlet [Filter] implementation to trace incoming HTTP request with [M3Tracer].
 *
 * Similar to OcHttpServletFilter (opencensus-contrib-http-servlet) but differs in following aspects:
 * - Simple : Wraps OpenCensus details. Dropped some configurations that not needed / misreading for our use-case.
 * - Fail safe : Always run application logic even if any type of error occurs in this filter.
 * - Do not break request : this filter do not touch request data to keep `setCharacterEncoding()` working well.
 * - Generic : Not only for OpenCensus but also capable with other tracing SDKs.
 */
open class M3TracingFilter: Filter {
    companion object {
        private val logger = LoggerFactory.getLogger(M3TracingFilter::class.java)
    }

    private var initCalled = false
    @VisibleForTesting
    internal var config: Config = Config()

    @ThreadSafe
    data class Config(
            val tracer: M3Tracer = M3TracerFactory.get(),
            val shutdownTracer: Boolean = true,
            val isOldServletVersion: Boolean = false
    ) {
        companion object {
            fun fromFilterConfig(filterConfig: FilterConfig) = Config(
                    shutdownTracer = (filterConfig.getInitParameter("shutdown_tracer") ?: "true").toBoolean(),
                    isOldServletVersion = filterConfig.servletContext.majorVersion < 3
            )
        }
    }

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        if (this.initCalled) {
            // Should ignore double-init.
            // Especially for spring-boot, init(Config) is called to set config by-instance.
            // Thus by-string based FilterConfig should be ignored.
            return
        }

        this.initCalled = true
        this.config = Config.fromFilterConfig(filterConfig)
    }
    fun init(config: Config) {
        if (this.initCalled) {
            return
        }

        this.initCalled = true
        this.config = config
    }

    override fun destroy() {
        if (this.config.shutdownTracer) {
            this.config.tracer.close()
        }
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(rawReq: ServletRequest, rawRes: ServletResponse, chain: FilterChain) {
        val req = rawReq as? HttpServletRequest
        val res = rawRes as? HttpServletResponse
        if (req == null || res == null) {
            chain.doFilter(rawReq, rawRes)
            return
        }

        val chainInvoked = AtomicBoolean(false)
        var chainError: Throwable? = null
        try {
            this.config.tracer.processIncomingHttpRequest(wrapRequest(req)).use { span ->
                chainInvoked.set(true)
                chainError = chain.doFilterAndCatch(rawReq, rawRes)
                span.setError(chainError)

                span.setResponse(wrapResponse(res))
            }
        } catch (e: Exception) {
            logger.error("Error occured in tracing", e)
        } finally {
            // Always run application logic even if tracing failed
            if (! chainInvoked.get()) {
                chainInvoked.set(true)
                chainError = chain.doFilterAndCatch(rawReq, rawRes)
            }
        }
        if (chainError != null) {
            throw chainError!! // Pass-through application exception
        }
    }

    private fun FilterChain.doFilterAndCatch(req: ServletRequest, res: ServletResponse): Throwable? {
        try {
            this.doFilter(req, res)
        } catch (e: Throwable) {
            return e
        }
        return null
    }

    protected open fun wrapRequest(req: HttpServletRequest) = ServletHttpRequestInfo(req)
    protected open fun wrapResponse(res: HttpServletResponse) = ServletHttpResponseInfo(res, this.config.isOldServletVersion)
}
