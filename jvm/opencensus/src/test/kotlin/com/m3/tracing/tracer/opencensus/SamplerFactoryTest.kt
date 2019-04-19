package com.m3.tracing.tracer.opencensus

import com.google.common.truth.Truth
import io.opencensus.trace.samplers.Samplers
import org.junit.jupiter.api.Test

class SamplerFactoryTest {

    @Test
    fun testAlways() {
        Truth.assertThat(SamplerFactory.createSampler("always")).isEqualTo(Samplers.alwaysSample())
    }

    @Test
    fun testNever() {
        Truth.assertThat(SamplerFactory.createSampler("never")).isEqualTo(Samplers.neverSample())
    }

    @Test
    fun testRatio() {
        Truth.assertThat(SamplerFactory.createSampler("0.5").description).isEqualTo("ProbabilitySampler{0.500000}")
    }

    @Test
    fun testInvalidRatio() {
        Truth.assertThat(SamplerFactory.createSampler("0xff")).isEqualTo(Samplers.neverSample())
    }
}