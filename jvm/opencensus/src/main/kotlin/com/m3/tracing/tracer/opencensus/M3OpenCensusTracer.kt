package com.m3.tracing.tracer.opencensus

import com.m3.tracing.M3Tracer
import com.m3.tracing.NoopSpan
import com.m3.tracing.TraceContext
import com.m3.tracing.TraceSpan
import com.m3.tracing.http.HttpRequestInfo
import com.m3.tracing.http.HttpRequestSpan
import com.m3.tracing.internal.Config
import io.grpc.Context
import io.opencensus.common.Scope
import io.opencensus.trace.Span
import io.opencensus.trace.Tracer
import io.opencensus.trace.Tracing
import io.opencensus.trace.config.TraceConfig
import io.opencensus.trace.export.ExportComponent
import org.slf4j.LoggerFactory

/**
 * Send tracing data to somewhere with OpenTracing.
 * Note that you need to register manually (e.g. `LoggingTraceExporter.register();`).
 */
class M3OpenCensusTracer internal constructor(
        val tracer: Tracer,
        val traceConfig: TraceConfig,
        val exportComponent: ExportComponent
) : M3Tracer {
    companion object {
        const val samplingConfigName = "m3.tracer.opencensus.sampling"

        private val logger = LoggerFactory.getLogger(M3OpenCensusTracer::class.java)
    }

    constructor(): this(
            tracer = Tracing.getTracer(),
            traceConfig = Tracing.getTraceConfig(),
            exportComponent = Tracing.getExportComponent()
    )

    private val sampler = createSampler()
    private val propagator = Tracing.getPropagationComponent()
    private val httpRequestTracer = HttpRequestTracer(tracer, propagator.traceContextFormat)

    init {
        setupOpenCensus()
        logger.info("Tracer started")
    }

    override fun close() {
        exportComponent.shutdown()
        logger.info("Tracer closed")
    }

    private fun setupOpenCensus() {
        Tracing.getTraceConfig().also { traceConfig ->
            traceConfig.updateActiveTraceParams(
                    traceConfig.activeTraceParams.toBuilder().setSampler(sampler).build()
            )
        }
    }

    override val currentContext: TraceContext
        get() = TraceContextImpl(tracer)

    override fun processIncomingHttpRequest(request: HttpRequestInfo): HttpRequestSpan = httpRequestTracer.processRequest(request)

    private fun createSampler() = SamplerFactory.createSampler(
            Config[samplingConfigName] ?: "never"
    )
}

internal class TraceContextImpl(
        private val tracer: Tracer,
        private val context: Context = Context.current()
): TraceContext {
    companion object {
        private val logger = LoggerFactory.getLogger(TraceContextImpl::class.java)
    }

    override fun startSpan(name: String): TraceSpan {
        try {
            val currentSpan = TraceSpanImpl.getCurrent(context)
            if (currentSpan != null) return currentSpan.startChildSpan(name)

            val span = tracer.spanBuilder(name).startSpan()
            // Must be same context with parent because OpenCensus stores some objects in gRPC context and expects it to be inherited over threads: https://github.com/GoogleCloudPlatform/cloud-trace-java/issues/85#issuecomment-440402234
            // context.attach() must BEFORE tracer.withSpan because it set current span into current context.
            val scopeParentContext = context.attach()
            val scope = tracer.withSpan(span)
            return object: TraceSpanImpl(
                    parentSpan = null
            ) {
                override val tracer: Tracer = this@TraceContextImpl.tracer
                override val span: Span = span
                override val scope: Scope = scope
                override val scopeParentContext = scopeParentContext
            }.also {
                it.init()
            }
        } catch (e: Exception) {
            logger.error("Failed to start span", e)
            return NoopSpan
        }
    }
}