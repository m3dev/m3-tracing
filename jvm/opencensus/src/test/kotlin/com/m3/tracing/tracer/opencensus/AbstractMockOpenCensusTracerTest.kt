package com.m3.tracing.tracer.opencensus

import io.opencensus.trace.Tracer
import io.opencensus.trace.config.TraceConfig
import io.opencensus.trace.export.ExportComponent
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
abstract class AbstractMockOpenCensusTracerTest {
    @Mock
    lateinit var tracer: Tracer

    @Mock
    lateinit var traceConfig: TraceConfig

    @Mock
    lateinit var exportComponent: ExportComponent

    val m3tracer by lazy { M3OpenCensusTracer(tracer, traceConfig, exportComponent) }
}
