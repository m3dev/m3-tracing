package com.m3.tracing

import com.google.common.truth.Truth
import org.junit.jupiter.api.Test

class NoopSpanTest {

    @Test
    fun `startChildSpan should return NoopSpan itself`() {
        Truth.assertThat(NoopSpan.startChildSpan("dummy")).isSameInstanceAs(NoopSpan)
    }

    @Test
    fun `Do nothing on close`() {
        NoopSpan.close()
    }

    @Test
    fun `Do nothing on set`() {
        Truth.assertThat(NoopSpan.set("tag", "value")).isSameInstanceAs(NoopSpan)
        Truth.assertThat(NoopSpan.set("tag", true)).isSameInstanceAs(NoopSpan)
        Truth.assertThat(NoopSpan.set("tag", 123L)).isSameInstanceAs(NoopSpan)
    }
}
