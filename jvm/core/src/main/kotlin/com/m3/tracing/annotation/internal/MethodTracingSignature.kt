package com.m3.tracing.annotation.internal

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.m3.tracing.annotation.M3Traced
import com.m3.tracing.annotation.M3TracedParameter
import java.lang.reflect.Method
import java.lang.reflect.Parameter

class MethodTracingSignature private constructor(
        private val method: Method,
        private val methodAnnotation: M3Traced
) {
    companion object {
        private val cache = CacheBuilder.newBuilder()
                .weakKeys()
                .build(CacheLoader.from<Method, MethodTracingSignature?> { method ->
                    if (method == null) return@from null
                    val methodAnnotation = method.getAnnotation(M3Traced::class.java) ?: return@from null
                    MethodTracingSignature(method, methodAnnotation)
                })

        fun get(method: Method) = cache[method]
    }

    val spanName = if (methodAnnotation.name.isNullOrEmpty()) "${method.declaringClass.name}#${method.name}" else methodAnnotation.name

    val argsTagMapping = method.parameters.mapIndexed { index, parameter ->
        val parameterAnnotation = parameter.getDeclaredAnnotation(M3TracedParameter::class.java)
                ?: return@mapIndexed null
        TagTracingSignature(index, parameter, parameterAnnotation)
    }.filterNotNull()

    class TagTracingSignature internal constructor(
            val index: Int,
            private val parameter: Parameter,
            private val parameterAnnotation: M3TracedParameter
    ) {
        val tagName = if (parameterAnnotation.tagName.isNullOrEmpty()) "${parameter.name}" else parameterAnnotation.tagName
    }
}