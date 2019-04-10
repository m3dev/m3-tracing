package com.m3.tracing.tracer.opencensus

import io.opencensus.trace.Sampler
import io.opencensus.trace.samplers.Samplers
import org.slf4j.LoggerFactory

internal object SamplerFactory {
    private val logger = LoggerFactory.getLogger(SamplerFactory::class.java)

    fun createSampler(config: String): Sampler {
        when(config.toLowerCase()) {
            "always" -> return Samplers.alwaysSample()
            "never" -> return Samplers.neverSample()
            else -> {
                val ratio = config.toDoubleOrNull()
                if (ratio != null) {
                    return Samplers.probabilitySampler(ratio)
                }
            }
        }

        logger.error("Invalid sampling specifier (fallback to neverSample): \"$config\"")
        return Samplers.neverSample()
    }
}