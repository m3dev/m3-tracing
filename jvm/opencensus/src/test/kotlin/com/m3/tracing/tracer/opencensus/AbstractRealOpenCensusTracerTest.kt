package com.m3.tracing.tracer.opencensus

import io.opencensus.trace.Tracing
import io.opencensus.trace.samplers.Samplers
import org.junit.jupiter.api.BeforeAll
import java.util.concurrent.atomic.AtomicBoolean

abstract class AbstractRealOpenCensusTracerTest {
    companion object {
        @JvmStatic
        protected val tracer = Tracing.getTracer()
        private val tracerStarted = AtomicBoolean(false)

        @BeforeAll
        @JvmStatic
        fun initTracer() {
            if (! tracerStarted.get()) return

            Tracing.getTraceConfig().also { traceConfig ->
                traceConfig.updateActiveTraceParams(
                        traceConfig.activeTraceParams.toBuilder().setSampler(Samplers.alwaysSample()).build()
                )
            }
            tracerStarted.set(true)
        }
    }
}