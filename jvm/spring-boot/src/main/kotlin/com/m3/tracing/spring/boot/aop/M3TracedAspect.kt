package com.m3.tracing.spring.boot.aop

import com.m3.tracing.M3Tracer
import com.m3.tracing.TraceSpan
import com.m3.tracing.annotation.M3Traced
import com.m3.tracing.annotation.internal.MethodTracingSignature
import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.reflect.MethodSignature

/**
 * AOP Aspect Implementation to trace DI/AOP managed object interactions.
 */
@Aspect
class M3TracedAspect(private val tracer: M3Tracer) {

    /**
     * Capture method call of [M3Traced] methods.
     */
    @Around("@annotation(${M3Traced.FQCN})")
    @Throws(Throwable::class)
    fun traceMethodCall(pjp: ProceedingJoinPoint): Any? {
        var proceeded = false
        try {
            val method = (pjp.signature as MethodSignature).method
            val signature = MethodTracingSignature.get(method)
            if (signature == null) {
                proceeded = true
                return pjp.proceed()
            }

            startSpan(signature, pjp.args).use { span ->
                try {
                    proceeded = true
                    return pjp.proceed()
                } catch (e: Throwable) {
                    span.setError(e)
                    throw e
                }
            }
        } finally {
            // Proceed anyway, even if tracing failed.
            if(! proceeded) return pjp.proceed()
        }
    }

    private fun startSpan(signature: MethodTracingSignature, args: Array<out Any?>): TraceSpan {
        return tracer.startSpan(signature.spanName).also { span ->
            signature.argsTagMapping.forEach { parameter ->
                span[parameter.tagName] = args[parameter.index]
            }
        }
    }
}
