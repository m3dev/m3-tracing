package com.m3.tracing.annotation

/**
 * Mark to trace this argument as a tag of span.
 * Should be used in conjunction with [M3Traced] annotation. Otherwise nothing happen.
 *
 * @param tagName Name of the tag. Empty to use default name.
 */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class M3TracedParameter(
        val tagName: String = ""
)
