package com.m3.tracing

import com.m3.tracing.M3TracerFactory.tracerFQCNEnvVarName
import com.m3.tracing.M3TracerFactory.tracerFQCNSystemPropertyName
import com.m3.tracing.tracer.logging.M3LoggingTracer
import org.slf4j.LoggerFactory
import javax.annotation.concurrent.ThreadSafe

/**
 * Singleton factory/holder of [M3Tracer] instance.
 *
 * To choose [M3Tracer] implementation, use one of following methods:
 * 1. Set FQCN of [M3Tracer] implementation in system property ([tracerFQCNSystemPropertyName])
 * 2. Set FQCN of [M3Tracer] implementation in environment variable ([tracerFQCNEnvVarName])
 */
@ThreadSafe
object M3TracerFactory {
    private const val tracerFQCNSystemPropertyName = "m3.tracer.fqcn"
    private const val tracerFQCNEnvVarName = "M3_TRACER_FQCN"

    private val logger = LoggerFactory.getLogger(M3TracerFactory::class.java)

    // Use SYNCHRONIZED mode to prevent double-initialization of tracer SDKs.
    private val tracer: M3Tracer by lazy(LazyThreadSafetyMode.SYNCHRONIZED) { createTracer() }

    /**
     * Get instance of [M3Tracer].
     */
    fun get() = tracer

    private fun createTracer(): M3Tracer {
        return createTracerByFQCN(System.getProperty(tracerFQCNSystemPropertyName))
                ?: createTracerByFQCN(System.getenv(tracerFQCNEnvVarName))
                ?: M3LoggingTracer()
    }
    private fun createTracerByFQCN(fqcn: String?): M3Tracer? {
        if (fqcn == null) return null

        try {
            @Suppress("UNCHECKED_CAST")
            val cls = Class.forName(fqcn) as Class<out M3Tracer>
            if (! M3Tracer::class.java.isAssignableFrom(cls)) {
                logger.error("Ignored invalid tracer FQCN (is not subclass of ${M3Tracer::class.java.name}): \"$fqcn\"")
                return null
            }

            val ctor = cls.getConstructor()
            return ctor.newInstance()
        } catch (e: ReflectiveOperationException) {
            // ClassNotFoundException, NoSuchMethodException, InvocationTargetException, ...
            logger.error("Ignored invalid tracer FQCN (${e.javaClass.simpleName}): \"$fqcn\"", e)
            return null
        }
    }
}
