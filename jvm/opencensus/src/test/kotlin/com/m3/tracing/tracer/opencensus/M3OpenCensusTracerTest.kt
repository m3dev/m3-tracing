package com.m3.tracing.tracer.opencensus

import com.google.common.truth.Truth
import com.m3.tracing.NoopSpan
import com.m3.tracing.http.HttpRequestInfo
import io.grpc.Context
import io.opencensus.trace.config.TraceConfig
import io.opencensus.trace.export.ExportComponent
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.util.concurrent.atomic.AtomicReference

class M3OpenCensusTracerTest {

    class `Test close`: AbstractMockOpenCensusTracerTest() {

        @Test
        fun `close should shutdown tracer`() {
            m3tracer.close()

            Mockito.verify(exportComponent, Mockito.only()).shutdown()
        }
    }

    @ExtendWith(MockitoExtension::class)
    class `Context inheritance test`: AbstractRealOpenCensusTracerTest() {

        @Mock
        lateinit var traceConfig: TraceConfig

        @Mock
        lateinit var exportComponent: ExportComponent

        val m3tracer by lazy { M3OpenCensusTracer(tracer, traceConfig, exportComponent) }

        @Test
        fun `Inherit across thread`() {
            val threadMarker = Context.key<String>("thread_marker")
            Context.current().withValue(threadMarker, "junit_thread").run {
                m3tracer.startSpan("test span").use { rootSpan ->
                    Truth.assertThat(threadMarker.get()).isEqualTo("junit_thread")
                    val parentThreadContext = m3tracer.currentContext

                    val exceptionInThread = AtomicReference<Throwable>()
                    Thread {
                        try {
                            val originalContextOfThread = Context.current()
                            Truth.assertThat(threadMarker.get()).isNull()
                            Truth.assertThat(TraceSpanImpl.getCurrent(Context.current())).isNull()

                            parentThreadContext.startSpan("child span").use {childSpan ->
                                Truth.assertThat(threadMarker.get()).isEqualTo("junit_thread")
                                Truth.assertThat(TraceSpanImpl.getCurrent(Context.current())).isEqualTo(childSpan)
                            }

                            Truth.assertThat(threadMarker.get()).isNull()
                            Truth.assertThat(Context.current()).isSameInstanceAs(originalContextOfThread)
                            Truth.assertThat(TraceSpanImpl.getCurrent(Context.current())).isNull()
                        } catch (e: Throwable) {
                            exceptionInThread.set(e)
                        }
                    }.also {
                        it.start()
                        it.join()
                    }
                    Truth.assertThat(exceptionInThread.get()).isNull()
                }
            }
        }
    }

    @ExtendWith(MockitoExtension::class)
    class `Http context test`: AbstractRealOpenCensusTracerTest() {

        @Mock
        lateinit var traceConfig: TraceConfig

        @Mock
        lateinit var exportComponent: ExportComponent

        @Mock
        lateinit var request: HttpRequestInfo

        val m3tracer by lazy { M3OpenCensusTracer(tracer, traceConfig, exportComponent) }

        @Test
        fun `test`() {
            val threadMarker = Context.key<String>("thread_marker")
            Context.current().withValue(threadMarker, "junit_thread").run {
                Truth.assertThat(TraceSpanImpl.getCurrent(Context.current())).isNull()
                Truth.assertThat(threadMarker.get()).isEqualTo("junit_thread")

                m3tracer.processIncomingHttpRequest(request).use { span ->
                    Truth.assertThat(threadMarker.get()).isEqualTo("junit_thread")
                    Truth.assertThat(TraceSpanImpl.getCurrent(Context.current())).isEqualTo(span)
                }

                Truth.assertThat(TraceSpanImpl.getCurrent(Context.current())).isNull()
                Truth.assertThat(threadMarker.get()).isEqualTo("junit_thread")
            }
        }
    }

    class `Exception handling test`: AbstractMockOpenCensusTracerTest() {

        @Test
        @Disabled
        fun `Tracer_startSpan should not throw exception`() {
            // Cannot raise exception in TraceContextImpl#startSpan because all method call are final/static method call (incompatible for mocking)
            Mockito.doThrow(RuntimeException("Test exception")).`when`(tracer).spanBuilder(Mockito.any())

            m3tracer.startSpan("invalid").use {
                Truth.assertThat(it).isEqualTo(NoopSpan)
            }
        }


        @Test
        @Disabled
        fun `TraceContext_startSpan should not throw exception`() {
            // Cannot raise exception in TraceContextImpl#startSpan because all method call are final/static method call (incompatible for mocking)
            Mockito.doThrow(RuntimeException("Test exception")).`when`(tracer).spanBuilder(Mockito.any())

            val context = m3tracer.currentContext
            context.startSpan("invalid").use {
                Truth.assertThat(it).isEqualTo(NoopSpan)
            }
        }
    }
}