package com.m3.tracing.annotation

/**
 * Mark method to trace method calls.
 *
 * Note that you need to enable additional library (e.g. spring-boot integration) to use this annotation. Otherwise this annotation do nothing.
 *
 * @param name Name of the span. Empty to use default name.
 */
@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
annotation class M3Traced(
        val name: String = ""
) {
    companion object {
        /**
         * FQCN of this class, useful to write AspectJ expressions.
         */
        const val FQCN = "com.m3.tracing.annotation.M3Traced"
    }
}
