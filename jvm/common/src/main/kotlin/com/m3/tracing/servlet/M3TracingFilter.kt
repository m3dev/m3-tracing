package com.m3.tracing.servlet

import com.m3.tracing.servlet.impl.ServletRequestExtractor
import io.opencensus.contrib.http.HttpExtractor
import io.opencensus.contrib.http.HttpServerHandler
import io.opencensus.trace.AttributeValue
import io.opencensus.trace.Span
import io.opencensus.trace.Tracing
import io.opencensus.trace.propagation.TextFormat
import io.opencensus.trace.samplers.Samplers
import org.slf4j.LoggerFactory
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean
import javax.servlet.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Servlet filter for distributed tracing.
 *
 * Similar to OcHttpServletFilter (opencensus-contrib-http-servlet) but differs in following aspects:
 * - Simple : Wraps OpenCensus details. Dropped some configurations that not needed / misreading for our use-case
 * - Fail safe : Always run application logic even if any type of error occurs in this filter
 * - Do not break request : this filter do not touch request data to keep `setCharacterEncoding()` working well
 */
class M3TracingFilter : Filter {
    companion object {
        private val logger = LoggerFactory.getLogger(M3TracingFilter::class.java)
    }

    private lateinit var propagator: TextFormat
    private lateinit var getter: TextFormat.Getter<HttpServletRequest>
    private lateinit var extractor: HttpExtractor<HttpServletRequest, HttpServletResponse>
    private lateinit var handler: HttpServerHandler<HttpServletRequest, HttpServletResponse, HttpServletRequest>

    private var shutdownTracer: Boolean = true
    private lateinit var requestHeadersToCapture: List<String>

    @Throws(ServletException::class)
    override fun init(filterConfig: FilterConfig) {
        val sampler = (filterConfig.getInitParameter("sampling_ratio") ?: "never").let {
            when (it) {
                "always" -> Samplers.alwaysSample()
                "never" -> Samplers.neverSample()
                else -> Samplers.probabilitySampler(it.toDouble())
            }
        }
        shutdownTracer = (filterConfig.getInitParameter("shutdown_tracer") ?: "true").toBoolean()
        requestHeadersToCapture = (filterConfig.getInitParameter("request_headers") ?: "X-Forwarded-For").let {
            it.split(",").map { it.trim() }.filter { !it.isNullOrEmpty() }
        }

        Tracing.getTraceConfig().also { traceConfig ->
            traceConfig.updateActiveTraceParams(traceConfig.activeTraceParams.toBuilder().setSampler(sampler).build())
        }

        propagator = Tracing.getPropagationComponent().traceContextFormat
        getter = object: TextFormat.Getter<HttpServletRequest>() {
            override fun get(carrier: HttpServletRequest, key: String): String? {
                return carrier.getHeader(key)
            }
        }
        extractor = ServletRequestExtractor()
        handler = HttpServerHandler(Tracing.getTracer(), extractor, propagator, getter, true)
    }

    override fun destroy() {
        if (shutdownTracer) {
            Tracing.getExportComponent().shutdown()
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
        var span: Span? = null
        try {
            try {
                val context = handler.handleStart(req, req)
                req.contentLength.let { length ->
                    if (length > 0) handler.handleMessageReceived(context, length.toLong())
                }
                span = handler.getSpanFromContext((context))
                Tracing.getTracer().withSpan(span).use { scope ->
                    // note: Must close scope to prevent leak!
                    chainInvoked.set(true)
                    chainError = chain.doFilterAndCatch(rawReq, rawRes)
                }
                if (chainError != null) saveException(span, chainError!!)
                res.getHeader("Content-Length").let { length ->
                    if (! length.isNullOrEmpty()) handler.handleMessageSent(context, length.toLong())
                }
                captureAdditionalRequestInfo(span, req)
                handler.handleEnd(context, req, res, chainError)
                span = null // Span is closed in handleEnd
            } catch (e: Exception) {
                logger.error("Error occured in tracing", e)
            } finally {
                // Always run application logic even if tracing failed
                if (! chainInvoked.get()) {
                    chainInvoked.set(true)
                    chainError = chain.doFilterAndCatch(rawReq, rawRes)
                }
                if (span != null) {
                    span.end() // Close span to prevent memory leak
                }
            }
        } finally {
            if (chainError != null) {
                throw chainError!! // Re-throw application exception anyway
            }
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

    private fun captureAdditionalRequestInfo(span: Span, req: HttpServletRequest) {
        span.putAttribute("remote_addr", req.remoteAddr)

        requestHeadersToCapture.forEach { header ->
            span.putAttribute("x_forwarded_for", req.getHeader(header))
        }

        // note: Do not call req.getParameter even if this method is called after FilterChain.
        // Because servlet might do "forward" request and destination serlvet may call setCharacterEncoding().
        // Thus this filter should not do anything breaks setCharacterEncoding() even after FilterCain.
    }
    private fun saveException(span: Span, e: Throwable) {
        span.putAttribute("exception_class", e.javaClass.name)
        span.putAttribute("exception_messsage", e.message)
    }

    private fun Span.putAttribute(key: String, value: String?) {
        if (value == null) return
        this.putAttribute(key, AttributeValue.stringAttributeValue(value))
    }
}
