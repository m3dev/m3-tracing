package com.m3.tracing.tracer.opencensus

import com.m3.tracing.M3Tracer
import com.m3.tracing.http.HttpRequestInfo
import com.m3.tracing.http.HttpRequestSpan
import io.opencensus.trace.Tracing
import org.slf4j.LoggerFactory

/**
 * Send tracing data to somewhere with OpenTracing.
 * Note that you need to register manually (e.g. `LoggingTraceExporter.register();`).
 */
class M3OpenCensusTracer : M3Tracer {
    companion object {
        const val samplingSystemPropertyName = "m3.tracer.opencensus.sampling"
        const val samplingEnvVarName = "M3_TRACER_OPENCENSUS_SAMPLING"

        private val logger = LoggerFactory.getLogger(M3OpenCensusTracer::class.java)
    }

    private val tracer = Tracing.getTracer()
    private val sampler = createSampler()
    private val propagator = Tracing.getPropagationComponent()
    private val httpRequestTracer = HttpRequestTracer(tracer, propagator.traceContextFormat)

    init {
        setupOpenCensus()
        logger.info("Tracer started")
    }

    override fun close() {
        Tracing.getExportComponent().shutdown()
        logger.info("Tracer closed")
    }

    private fun setupOpenCensus() {
        Tracing.getTraceConfig().also { traceConfig ->
            traceConfig.updateActiveTraceParams(
                    traceConfig.activeTraceParams.toBuilder().setSampler(sampler).build()
            )
        }
    }

    override fun processIncomingHttpRequest(request: HttpRequestInfo): HttpRequestSpan = httpRequestTracer.processRequest(request)

    private fun createSampler() = SamplerFactory.createSampler(System.getProperty(samplingSystemPropertyName)
            ?: System.getenv(samplingEnvVarName)
            ?: "never")
}
