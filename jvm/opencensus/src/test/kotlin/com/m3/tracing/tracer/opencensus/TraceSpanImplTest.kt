package com.m3.tracing.tracer.opencensus

import com.google.common.truth.Truth
import com.m3.tracing.NoopSpan
import io.grpc.Context
import io.opencensus.common.Scope
import io.opencensus.trace.AttributeValue
import io.opencensus.trace.Span
import io.opencensus.trace.Tracer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension
import java.util.concurrent.atomic.AtomicReference

class TraceSpanImplTest {
    companion object {
        private val contextMarkerKey = Context.key<String>("Context Marker")
    }

    class `companion object` {

        @Test
        fun `currentSpan without Context`() {
            Truth.assertThat(TraceSpanImpl.getCurrent(Context.ROOT)).isNull()
        }

        @Test
        fun `closeQuietly without error`() {
            var called = false
            TraceSpanImpl.closeQuietly {
                called = true
            }
            Truth.assertThat(called).isTrue()
        }

        @Test
        fun `closeQuietly with error`() {
            TraceSpanImpl.closeQuietly { throw RuntimeException("Test exception") }
        }
    }

    @ExtendWith(MockitoExtension::class)
    class `tag setters` {
        @Mock
        private lateinit var span: Span

        private val spanImpl by lazy {
            object: TraceSpanImpl(null){
                override val tracer: Tracer
                    get() = TODO("not implemented")
                override val span: Span
                    get() = this@`tag setters`.span
                override val scope: Scope?
                    get() = TODO("not implemented")
                override val scopeParentContext: Context?
                    get() = null
            }
        }

        @Test
        fun `set non-null tags`() {
            spanImpl["string_tag"] = "string value"
            spanImpl["empty_string_tag"] = ""
            spanImpl["boolean_tag"] = true
            spanImpl["int_tag"] = 123
            spanImpl["long_tag"] = 123456L

            Mockito.verify(span).putAttribute("string_tag", AttributeValue.stringAttributeValue("string value"))
            Mockito.verify(span).putAttribute("empty_string_tag", AttributeValue.stringAttributeValue(""))
            Mockito.verify(span).putAttribute("boolean_tag", AttributeValue.booleanAttributeValue(true))
            Mockito.verify(span).putAttribute("int_tag", AttributeValue.longAttributeValue(123))
            Mockito.verify(span).putAttribute("long_tag", AttributeValue.longAttributeValue(123456L))
        }

        @Test
        fun `set null tags`() {
            spanImpl["string_tag"] = null as String?
            spanImpl["boolean_tag"] = null as Boolean?
            spanImpl["int_long_tagtag"] = null as Long?

            Mockito.verify(span, Mockito.never()).putAttribute(Mockito.any(), Mockito.any())
        }
    }

    @ExtendWith(MockitoExtension::class)
    class `error handling tests` {

        @Mock
        lateinit var tracer: Tracer

        @Mock
        lateinit var span: Span

        private val spanImpl by lazy {
            object: TraceSpanImpl(null){
                override val tracer: Tracer
                    get() = this@`error handling tests`.tracer
                override val span: Span
                    get() = this@`error handling tests`.span
                override val scope: Scope?
                    get() = TODO("not implemented")
                override val scopeParentContext: Context?
                    get() = null
            }
        }

        @Test
        fun `startChildSpan should return NoopSpan on error`() {
            // Throw exception on the start of startChildSpan()
            Mockito.`when`(tracer.spanBuilderWithExplicitParent(Mockito.any(), Mockito.any())).thenThrow(RuntimeException("Test exception"))

            Truth.assertThat(spanImpl.startChildSpan("test span")).isEqualTo(NoopSpan)
        }
    }

    @ExtendWith(MockitoExtension::class)
    class `context inheritance`: AbstractRealOpenCensusTracerTest() {
        @Mock
        private lateinit var span: Span

        private val parentSpan by lazy {
            object: TraceSpanImpl(null){
                override val tracer: Tracer
                    get() = AbstractRealOpenCensusTracerTest.tracer
                override val span: Span
                    get() = TODO("not implemented")
                override val scope: Scope?
                    get() = TODO("not implemented")
                override val scopeParentContext: Context?
                    get() = null
            }
        }

        @Test
        fun `Context lifecycle between parentContext and this context and child context`() {
            Context.ROOT.withValue(contextMarkerKey, "outer").run {
                val outMostContext = Context.current()

                val spanImpl = TraceSpanImpl.NonRootSpan(parentSpan, span, scope = null, scopeParentContext = null)
                Truth.assertWithMessage("Before init(), context should not be changed").that(Context.current()).isEqualTo(outMostContext)
                Truth.assertThat(TraceSpanImpl.getCurrent(Context.current())).isNull()

                spanImpl.init()
                try {
                    val spanContext = Context.current()
                    Truth.assertWithMessage("After init(), context should be changed").that(spanContext).isNotEqualTo(outMostContext)
                    Truth.assertWithMessage("Context should be child of the caller context").that(contextMarkerKey.get()).isEqualTo("outer")
                    Truth.assertWithMessage("After init(), should be able to get current span").that(TraceSpanImpl.getCurrent(Context.current())).isEqualTo(spanImpl)

                    spanImpl.startChildSpan("child_span").use { childSpan ->
                        Truth.assertWithMessage("Child span must create child context").that(Context.current()).isNotEqualTo(spanContext)
                        Truth.assertWithMessage("Current span must be changed").that(TraceSpanImpl.getCurrent(Context.current())).isEqualTo(childSpan)
                    }

                    Truth.assertWithMessage("close() of child span must revert context").that(Context.current()).isEqualTo(spanContext)
                    Truth.assertWithMessage("close() of child span must current span").that(TraceSpanImpl.getCurrent(Context.current())).isEqualTo(spanImpl)
                } finally {
                    spanImpl.close()
                }

                Truth.assertWithMessage("After close(), context should be backed").that(Context.current()).isEqualTo(outMostContext)
                Truth.assertThat(TraceSpanImpl.getCurrent(Context.current())).isNull()
            }

        }

        @Test
        fun `Span in separate thread must propagate gRPC context`() {
            Context.current().withValue(contextMarkerKey, "junit_thread").run {
                val spanImpl = TraceSpanImpl.NonRootSpan(parentSpan, span, scope = null, scopeParentContext = null)
                spanImpl.init()
                try {
                    val threadError = AtomicReference<Throwable>()
                    val thread = Thread {
                        try {
                            Truth.assertWithMessage("Child thread context should not have context marker in the start of thread").that(contextMarkerKey.get()).isNull()
                            Truth.assertThat(TraceSpanImpl.getCurrent(Context.current())).isNull()
                            val rootContextOfThread = Context.current().withValue(contextMarkerKey, "child_thread").run {
                                Truth.assertThat(contextMarkerKey.get()).isEqualTo("child_thread")
                                spanImpl.startChildSpan("child_span").use { childSpan ->
                                    Truth.assertWithMessage("Context should inherit span owner's context, not child context").that(contextMarkerKey.get()).isEqualTo("junit_thread")
                                    Truth.assertThat(TraceSpanImpl.getCurrent(Context.current())).isEqualTo(childSpan)
                                }
                                Truth.assertWithMessage("After span.close(), it should revert Context to original Context of the thread.").that(contextMarkerKey.get()).isEqualTo("child_thread")
                                Truth.assertThat(TraceSpanImpl.getCurrent(Context.current())).isNull()
                            }
                        } catch (e: Throwable) {
                            threadError.set(e)
                        }
                    }.also {
                        it.start()
                        it.join()
                    }
                    Truth.assertWithMessage("Test in thread should succeed").that(threadError.get()).isNull()
                } finally {
                    spanImpl.close()
                }
            }
        }
    }

}