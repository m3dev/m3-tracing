package com.m3.tracing.tracer.opencensus

import io.opencensus.trace.AttributeValue
import io.opencensus.trace.Span
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class SpanUtilsTest {

    @Mock
    lateinit var span: Span

    @Test
    fun `putAttribute should ignore null`() {
        span.putAttribute("key", null as String?)

        Mockito.verify(span, Mockito.never()).putAttribute(Mockito.any(), Mockito.any())
    }

    @Test
    fun `putAttribute should pass non-null string`() {
        span.putAttribute("key", "string value")
        span.putAttribute("empty_key", "")

        Mockito.verify(span).putAttribute("key", AttributeValue.stringAttributeValue("string value"))
        Mockito.verify(span).putAttribute("empty_key", AttributeValue.stringAttributeValue(""))
    }
}

