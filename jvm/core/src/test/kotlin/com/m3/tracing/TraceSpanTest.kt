package com.m3.tracing

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito
import org.mockito.Spy
import org.mockito.junit.jupiter.MockitoExtension

@ExtendWith(MockitoExtension::class)
class TraceSpanTest {

    @Spy
    lateinit var mockSpan: TraceSpan

    @Test
    fun `setError should set tags`() {
        mockSpan.setError(IllegalArgumentException("test exception message"))

        Mockito.verify(mockSpan).set("exception_class", "java.lang.IllegalArgumentException")
        Mockito.verify(mockSpan).set("exception_messsage", "test exception message")
    }

    @Test
    fun `set(String, null) should do nothing`() {
        val tagName = "dummy_tag"
        mockSpan[tagName] = null as Any?

        Mockito.verify(mockSpan, Mockito.never()).set(tagName, null as String?)
        Mockito.verify(mockSpan, Mockito.never()).set(tagName, null as Boolean?)
        Mockito.verify(mockSpan, Mockito.never()).set(tagName, null as Long?)
    }

    @Test
    fun `set(String, Object) must set tag`() {
        mockSpan["string_tag"] = "string value"
        mockSpan["boolean_tag"] = true
        mockSpan["long_tag"] = 1234L

        Mockito.verify(mockSpan).set("string_tag", "string value")
        Mockito.verify(mockSpan).set("boolean_tag", true)
        Mockito.verify(mockSpan).set("long_tag", 1234L)
    }
}